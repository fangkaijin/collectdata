package com.aaron.petsocial.im

import android.content.Context
import com.hyphenate.EMCallBack
import com.hyphenate.EMConnectionListener
import com.hyphenate.EMMessageListener
import com.hyphenate.EMValueCallBack
import com.hyphenate.chat.EMClient
import com.hyphenate.chat.EMConversation
import com.hyphenate.chat.EMCursorResult
import com.hyphenate.chat.EMFetchMessageOption
import com.hyphenate.chat.EMLoginExtensionInfo
import com.hyphenate.chat.EMMessage
import com.hyphenate.chat.EMOptions
import com.hyphenate.exceptions.HyphenateException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class HXIMHelper private constructor(){


    //初始化
    fun initIM(context: Context){

        val option: EMOptions = EMOptions()
        option.appKey = "1133241204169366#petsocial"
        option.autoLogin = true

        EMClient.getInstance().init(context, option)

        //设置日志
        EMClient.getInstance().setDebugMode(false)
    }

    //创建账号
    suspend fun createAccountTest(accountName: String, psw: String, createStatus: (tips: String?) -> Unit){

        try{


            EMClient.getInstance().createAccount(accountName, psw)

            withContext(Dispatchers.Main){
                createStatus("创建成功")
            }

        }catch (e: HyphenateException){
            e.printStackTrace()

            withContext(Dispatchers.Main){
                createStatus("创建失败 "+if(null==e) "" else (""+e.errorCode + "-"+e.message))
            }
        }
    }

    //创建账号，和接口交互
    fun createAccountByHttp(accountName: String, psw: String, nickName: String = "默认用户"){

        //可以考虑服务端调用接口
        //POST https://{host}/{org_name}/{app_name}/users
        //org_name 1133241204169366
        //app_name  petsocial

        //host  需要升级服务，

    }

    //登录账号
    fun loginPsw(accountName: String,
                 psw: String,
                 loginSuccess: (tips: String) -> Unit,
                 loginFailed: (code: Int, tips: String?) -> Unit,
                 loginProgress: (progress: Int, status: String?) -> Unit){

        EMClient.getInstance().login(accountName, psw, object : EMCallBack{
            override fun onSuccess() {

                loginSuccess("登录成功")

            }

            override fun onError(p0: Int, p1: String?) {
                loginFailed(p0, p1)
            }

            override fun onProgress(progress: Int, status: String?) {
                super.onProgress(progress, status)
                loginProgress(progress, status)
            }

        })
    }

    //token登录
    fun loginToken(accountName: String,
                   token: String,
                   loginSuccess: (tips: String) -> Unit,
                   loginFailed: (code: Int, tips: String?) -> Unit,
                   loginProgress: (progress: Int, status: String?) -> Unit){
        EMClient.getInstance().loginWithToken(accountName, token, object : EMCallBack{
            override fun onSuccess() {

                loginSuccess("登录成功")

            }

            override fun onError(p0: Int, p1: String?) {
                loginFailed(p0, p1)
            }

            override fun onProgress(progress: Int, status: String?) {
                super.onProgress(progress, status)
                loginProgress(progress, status)
            }

        })
    }

    //登录后刷新数据
    fun flushAfterLogin(){
        EMClient.getInstance().chatManager().loadAllConversations()
        EMClient.getInstance().groupManager().loadAllGroups()
    }

    //发生一条单聊消息
    fun sendSingleMsg(accountName: String,
                      content: String,
                      isSingle: Boolean = true,
                      sendSuccess: (tips: String) -> Unit,
                      sendFailed: (code: Int, tiips: String?) -> Unit,
                      sendProgress: (progress: Int, status: String?) -> Unit){
        val message: EMMessage = EMMessage.createTextSendMessage(content, accountName)
        message.chatType = if(isSingle) EMMessage.ChatType.Chat else EMMessage.ChatType.GroupChat

        //发送回调
        message.setMessageStatusCallback(object : EMCallBack{
            override fun onSuccess() {
                sendSuccess("发送成功")
            }

            override fun onError(p0: Int, p1: String?) {
                sendFailed(p0, p1)
            }

            override fun onProgress(progress: Int, status: String?) {
                sendProgress(progress, status)
            }

        })

        EMClient.getInstance().chatManager().sendMessage(message)
    }

    //从服务端获取你是消息
    fun queryHistoryChatFormServer(conversationId: String = "",
                                   type: EMConversation.EMConversationType = EMConversation.EMConversationType.Chat,
                                   pageSize: Int = 30,
                                   cursor: String = "",
                                   option: EMFetchMessageOption = EMFetchMessageOption(),
                                   querySuccess: (data: EMCursorResult<EMMessage>?) -> Unit,
                                   queryFailed: (code: Int, tips: String?) -> Unit
    ){

        EMClient.getInstance().chatManager().asyncFetchHistoryMessages(conversationId,
            type, pageSize, cursor, option, object : EMValueCallBack<EMCursorResult<EMMessage>>{
                override fun onSuccess(p0: EMCursorResult<EMMessage>?) {
                    querySuccess(p0)
                }

                override fun onError(p0: Int, p1: String?) {
                    queryFailed(p0, p1)
                }

            })

    }

    //获取服务端会话
    fun queryConversationFormServer(pageSize: Int = 30,
                             cursor: String = "",
                             querySuccess: (data: EMCursorResult<EMConversation>?) -> Unit,
                             queryFailed: (code: Int, tips: String?) -> Unit){

        EMClient.getInstance().chatManager().asyncFetchConversationsFromServer(pageSize, cursor, object : EMValueCallBack<EMCursorResult<EMConversation>>{
            override fun onSuccess(p0: EMCursorResult<EMConversation>?) {
                querySuccess(p0)
            }

            override fun onError(p0: Int, p1: String?) {
                queryFailed(p0, p1)
            }

        })
    }

    //获取本地会话
    suspend fun queryConversationFormLocal(): List<EMConversation>{

        return EMClient.getInstance().chatManager().getAllConversationsBySort()
    }

    fun queryHistoryChatFormLoal(accountName: String, type:EMConversation.EMConversationType = EMConversation.EMConversationType.Chat, createIfNotExists: Boolean = false): List<EMMessage>?{

        val conversation: EMConversation? = EMClient.getInstance().chatManager().getConversation(accountName, type, createIfNotExists)
        return conversation?.allMessages

    }

    //接收消息
    fun receivedMsg(onMessageReceived: (messages: MutableList<EMMessage>?) -> Unit){

        if(null!=msgListener){
            EMClient.getInstance().chatManager().removeMessageListener(msgListener)
            msgListener = null;
        }

        msgListener = object : EMMessageListener{
            override fun onMessageReceived(p0: MutableList<EMMessage>?) {

                onMessageReceived(p0)

            }


        }

        EMClient.getInstance().chatManager().addMessageListener(msgListener)
    }

    //取消消息监听
    fun removeMessageListener(){

        if(null!=msgListener){
            EMClient.getInstance().chatManager().removeMessageListener(msgListener)
            msgListener = null;
        }
    }

    //退出登录
    suspend fun logout(): Int{
        return EMClient.getInstance().logout(true)
    }

    //设置连接状态
    fun setConnectStatus(connectSuccess: (tips: String) -> Unit,
                         disConnect: (code: Int) -> Unit,
                         logout: (code: Int, info: EMLoginExtensionInfo?) -> Unit,
                         tokenExpired: () -> Unit,
                         tokenWillExpire: () -> Unit){
        if(null!= connectionListener){

            EMClient.getInstance().removeConnectionListener(connectionListener)
            connectionListener = null;
        }

        connectionListener = object : EMConnectionListener{
            override fun onConnected() {

                //连接
                connectSuccess("连接成功")

            }

            override fun onDisconnected(p0: Int) {
                //断开连接
                disConnect(p0)

            }

            override fun onLogout(errorCode: Int, info: EMLoginExtensionInfo?) {
                super.onLogout(errorCode, info)
                //登出
                logout(errorCode, info)

                //USER_LOGIN_ANOTHER_DEVICE=206: 用户已经在其他设备登录
                //USER_REMOVED=207: 用户账户已经被移除
                //USER_BIND_ANOTHER_DEVICE=213: 用户已经绑定其他设备
                //USER_LOGIN_TOO_MANY_DEVICES=214: 用户登录设备超出数量限制
                //USER_KICKED_BY_CHANGE_PASSWORD=216: 由于密码变更被踢下线
                //USER_KICKED_BY_OTHER_DEVICE=217: 由于其他设备登录被踢下线
                //USER_DEVICE_CHANGED=220: 和上次设备不同导致下线
                //SERVER_SERVICE_RESTRICTED=305: Chat 功能限制

            }

            override fun onTokenExpired() {
                super.onTokenExpired()
                //token 过期
                tokenExpired()
            }

            override fun onTokenWillExpire() {
                super.onTokenWillExpire()
                //token 过期
                tokenWillExpire()
            }

        }


        EMClient.getInstance().addConnectionListener(connectionListener)

    }

    //移除连接状态
    fun removeConnectStatus(){

        if(null!=connectionListener){

            EMClient.getInstance().removeConnectionListener(connectionListener)
            connectionListener = null;
        }
    }




    companion object{

        private var instance: HXIMHelper? = null

        var msgListener: EMMessageListener? = null
        var connectionListener: EMConnectionListener? = null

        @JvmStatic
        fun getInstance(): HXIMHelper{
            return instance?: synchronized(this){
                instance?:HXIMHelper().also { instance = it }
            }

        }

    }

}