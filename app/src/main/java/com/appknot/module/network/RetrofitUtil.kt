package com.appknot.module.network


import android.annotation.SuppressLint
import android.content.Context
import android.os.AsyncTask
import android.widget.ProgressBar
import com.appknot.seotda.extensions.hideLoadingDialog
import com.appknot.seotda.extensions.showLoadingDialog
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.internal.LinkedTreeMap
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.*
import java.util.*

/**
 *
 * @author Jin on 2019-08-13
 */
class RetrofitUtil {
    fun <T> create(apiInterface: Class<T>): T = retrofit.create(apiInterface)
    fun <T> create(apiInterface: Class<T>, baseUrl: String): T =
        build(baseUrl).create(apiInterface)

    var call: Call<ApiResponse>? = null
    var fileCall: Call<ResponseBody>? = null
    private var context: Context? = null
    private var progressVisibility = false
    private var successListener: ((Any) -> Unit)? = null
    private var errorListener: ((String, String) -> Unit)? = null
    private var failureListener: (() -> Unit)? = null

    /**
     * 통신 요청 수행
     */
    fun execute() {
        call?.enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                hideProgress()

                when (response.code()) {
                    // 작업 성공
                    200 -> {
                        response.body()?.run {
                            when (this.code) {
                                CODE_SUCCESS -> successListener?.invoke(this.data)
                                else -> errorListener?.invoke(this.code, this.msg.ko)
                            }
                        }
                    }
                    // 작업 실패 (404 Page not found 등...)
                    else -> onFailure(call, Throwable("Http response Error"))
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                hideProgress()
                failureListener?.invoke()
            }
        })
    }

    /**
     *  통신 요청 수행 with ProgressBar
     */
    fun executeWithProgress(context: Context) {
//        if (context is BaseActivity) {
        this.context = context
        context.showLoadingDialog()
        progressVisibility = true
//        }
        execute()
    }

    /**
     * 다운로드 수행
     * (NOTE: 메모리 상에서 처리할 수 있을만한 작은 크기의 파일을 다운 받을때만 기본 호출로 쓸 것.
     * 용량이 큰 파일은 스트리밍 모드를 활성화하여 호출해야 한다.)
     * @param filePath 저장할 파일명을 포함한 전체 경로
     * @param isStreaming 스트리밍 모드 활성화 여부 (용량 큰 파일 다운로드할 땐 true)
     */
    fun download(filePath: String, isStreaming: Boolean = false, pb: ProgressBar? = null) {
        fileCall?.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                hideProgress()

                when (response.code()) {
                    // 작업 성공
                    200 -> if (response.isSuccessful) {
                        response.body()?.let {
                            if (isStreaming) FileDownloader(it, filePath, pb).execute()
                            else copyStreamToDisk(it, filePath)
                        }
                    }

                    // 작업 실패
                    else -> onFailure(call, Throwable("Http response Error"))
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                hideProgress()
                failureListener?.invoke()
            }
        })
    }

    /**
     *  다운로드 수행 with ProgressBar
     *  (NOTE: 메모리 상에서 처리할 수 있을만한 작은 크기의 파일을 다운 받을때 쓸 것)
     *  @param filePath 저장할 파일명을 포함한 전체 경로
     */
    fun downloadWithProgress(context: Context, filePath: String) {
//        if (context is BaseActivity) {
        this.context = context
        context.showLoadingDialog()
        progressVisibility = true
//        }
        download(filePath)
    }

    /**
     *  다운로드 수행 with Streaming
     *  (NOTE: 대용량 파일을 다운로드 받을 때 쓸 것)
     *  @param filePath 저장할 파일명을 포함한 전체 경로
     *  @param pb 다운로드 진행률을 표시할 ProgressBar
     */
    fun downloadOnStreaming(filePath: String, pb: ProgressBar? = null) {
        download(filePath, true, pb)
    }

    /**
     * JSON 정보 담고 있는 맵을 자바 객체로 변환
     * @param data JSON 정보를 담고 있는 맵 객체
     * @param modelType 결과를 담을 자바 객체 클래스 타입
     * @return 변환 완료된 자바 객체 클래스 타입의 인스턴스
     */
    fun <T> parse(data: Any, modelType: Class<T>): T {
        val jsonStr = Gson().toJson(data)
        return Gson().fromJson(jsonStr, modelType)
    }

    fun <T> parse(jsonStr: String, modelType: Class<T>): T {
        return Gson().fromJson(jsonStr, modelType)
    }

    /**
     * JSON 정보 담고 있는 맵 형식 객체를 자바 객체 콜렉션으로 변환
     * @param data JSON 정보를 담고 있는 맵 객체
     * @param modelType 결과를 담을 자바 객체 배열 클래스 타입
     * @return 변환 완료된 자바 객체 클래스 타입의 콜렉션 인스턴스
     */
    fun <T> parse(data: Any, modelType: Class<Array<T>>): ArrayList<T> {
        val jsonStr = Gson().toJson(data)
        val resultArr = Gson().fromJson(jsonStr, modelType)
        return ArrayList(resultArr.asList())
    }

    fun toMap(data: Any): LinkedTreeMap<*, *> {
        return try {
            data as LinkedTreeMap<*, *>
        } catch (e: ClassCastException) {
            LinkedTreeMap<String, Any>()
        }
    }

    fun toList(data: Any): ArrayList<*> {
        return try {
            data as ArrayList<*>
        } catch (e: ClassCastException) {
            ArrayList<String>()
        }
    }

    /**
     * 수행중인 모든 요청을 취소한다.
     * (NOTE: 대용량 파일 다운로드를 취소하려면 이 함수를 활용할 것)
     */
    fun cancel() {
        call?.cancel()
        fileCall?.cancel()
    }

    fun onSuccess(body: (data: Any) -> Unit): RetrofitUtil {
        successListener = body
        return this
    }

    fun onError(body: (code: String, msg: String) -> Unit): RetrofitUtil {
        errorListener = body
        return this
    }

    fun onFailure(body: () -> Unit): RetrofitUtil {
        failureListener = body
        return this
    }

    private fun hideProgress() {
        if (progressVisibility) {
            context?.hideLoadingDialog()
            progressVisibility = false
        }
    }

    /* InputStream 을 파일로 저장한다. */
    private fun copyStreamToDisk(body: ResponseBody, filePath: String) {
        val savingFile = File(filePath)
        var outStream: OutputStream? = null
        try {
            outStream = FileOutputStream(savingFile)
            body.byteStream()?.copyTo(outStream)
        } catch (e: Exception) {
        } finally {
            outStream?.close()
            body.close()
        }
    }

    /* 대용량 파일 다운로더. ProgressBar 파라미터가 넘어온다면 다운로드 진행률도 표시한다. */
    @SuppressLint("StaticFieldLeak")
    inner class FileDownloader(
        private var body: ResponseBody,
        private var filePath: String,
        private var pb: ProgressBar?
    ) : AsyncTask<Void, Long, Void>() {

        override fun onPreExecute() {
            super.onPreExecute()

            pb?.max = body.contentLength().toInt()
        }

        override fun doInBackground(vararg params: Void): Void? {
            try {
                val savingFile = File(filePath)
                var inStream: InputStream? = null
                var outStream: OutputStream? = null

                try {
                    val fileReader = ByteArray(1024 * 8)
                    var fileSizeDownloaded = 0L

                    inStream = body.byteStream()
                    outStream = BufferedOutputStream(FileOutputStream(savingFile))

                    while (true) {
                        val data = inStream.read(fileReader)
                        if (data == -1) break
                        outStream.write(fileReader, 0, data)
                        fileSizeDownloaded += data.toLong()
                        publishProgress(fileSizeDownloaded)
                    }
                    outStream.flush()
                } catch (e: IOException) {
                } finally {
                    outStream?.close()
                    inStream?.close()
                }
            } catch (e: IOException) {
            }
            return null
        }

        override fun onProgressUpdate(vararg progress: Long?) {
            super.onProgressUpdate()

            pb?.let { it.progress = progress[0]?.toInt() ?: it.max }
        }

        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)

            // 다운로드 작업이 취소되었다면 success 리스너에 콜백을 보내지 않음
            fileCall?.let { if (it.isCanceled.not()) successListener?.invoke(filePath) }
        }
    }

    var isTestServer = true
    var API_HOST_TEST = ""
    var API_HOST = ""

    fun setIsTest(isTest: Boolean) {
        isTestServer = isTest
    }

    fun setHost(apiHost: String) {
        API_HOST = apiHost
    }

    fun setHostTest(apiHostTest: String) {
        API_HOST_TEST = apiHostTest
    }

    fun build(baseUrl: String): Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(
            GsonConverterFactory.create(GsonBuilder().setLenient().create())
        )
        .build()


    val CODE_SUCCESS = "0"

    private lateinit var retrofit: Retrofit


    fun rebuild(apiHost: String) {
        retrofit = build(apiHost)
    }

    fun setRetrofit(isTestServer: Boolean, apiHost: String, apiHostTest: String) {
        retrofit = build(
            when (isTestServer) {
                true -> apiHostTest
                false -> apiHost
            }
        )
    }

    companion object {
    }
}