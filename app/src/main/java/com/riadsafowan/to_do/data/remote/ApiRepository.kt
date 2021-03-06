package com.riadsafowan.to_do.data.remote

import com.riadsafowan.to_do.data.model.LoginRequest
import com.riadsafowan.to_do.data.model.posts.PostRequest
import com.riadsafowan.to_do.data.model.posts.comment.CommentRequest
import com.riadsafowan.to_do.data.model.signup.SignupRequest
import com.riadsafowan.to_do.data.model.task.TaskRequest
import okhttp3.MultipartBody
import javax.inject.Inject

open class ApiRepository @Inject constructor(
    private val apiClient: ApiClient,
) : SafeApiCall {

    suspend fun login(username: String, password: String) =
        safeApiCall { apiClient.login(LoginRequest(email = username, password = password)) }

    suspend fun signup(signupRequest: SignupRequest) =
        safeApiCall { apiClient.signup(signupRequest) }

    suspend fun addTask(taskRequest: TaskRequest) = safeApiCall { apiClient.addTask(taskRequest) }
    suspend fun addTasks(taskRequest: List<TaskRequest>) =
        safeApiCall { apiClient.addTasks(taskRequest) }

    suspend fun getTaskById(id: String) = safeApiCall { apiClient.getTaskById(id) }
    suspend fun getTasks() = safeApiCall { apiClient.getTasks() }
    suspend fun uploadProfileImage( image: MultipartBody.Part) = safeApiCall { apiClient.uploadProfileImage(image) }
    suspend fun getPosts( ) = safeApiCall { apiClient.getPosts() }
    suspend fun createPost(postRequest: PostRequest) = safeApiCall { apiClient.createPost(postRequest) }
    suspend fun uploadPostImage(image: MultipartBody.Part, id: Int) = safeApiCall { apiClient.uploadPostImage(image, id) }
    suspend fun likePost(id: Int)=safeApiCall { apiClient.likePost(id)}
    suspend fun getComments(id: Int)= safeApiCall { apiClient.getComments(id) }
    suspend fun createComment(id: Int, request: CommentRequest) = safeApiCall { apiClient.createComment(id,request) }
}