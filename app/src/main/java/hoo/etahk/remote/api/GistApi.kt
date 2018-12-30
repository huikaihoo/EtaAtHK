package hoo.etahk.remote.api

import hoo.etahk.remote.response.GistRes
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Url

// baseUrl = "https://api.github.com/gists/"
interface GistApi {
    @GET("{gist_id}")
    fun getGist(@Path("gist_id") gistId: String = ""): Call<GistRes>

    @GET
    fun getContent(@Url url: String): Call<ResponseBody>
}