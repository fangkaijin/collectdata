package com.aaron.petsocial.ui.fragment

import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.aaron.baselibs.base.BaseFragment
import com.aaron.baselibs.utils.showLog
import com.aaron.baselibs.utils.toast
import com.aaron.petsocial.adapter.ChatAdapter
import com.aaron.petsocial.databinding.LayoutFragmentTab3Binding
import com.aaron.petsocial.im.HXIMHelper
import com.hyphenate.chat.EMConversation
import com.hyphenate.chat.EMMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


/**
 * 聊天
 */
class HomeTab3Fragment: BaseFragment<LayoutFragmentTab3Binding>() {

    private val conversations: MutableList<EMConversation> = mutableListOf()

    private val chatAdapter: ChatAdapter by lazy {

        ChatAdapter(layoutInflater, conversations)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        "onCreateView 执行--".showLog()

        //设置接受消息监听
        HXIMHelper.getInstance().receivedMsg {

            if(null!=it && !it.isEmpty()){


                activity?.runOnUiThread {
                    "接收到消息--${it.get(0).from} -- ${it.get(0).to}".showLog()
                }

            }

        }
        HXIMHelper.getInstance().setConnectStatus({

                                                  activity?.runOnUiThread {

                                                      "已连接--$it".showLog()
                                                  }

            HXIMHelper.getInstance().flushAfterLogin()
        },{

            //断开连接
            activity?.runOnUiThread {

                "断开连接--$it".showLog()
            }

        }, {code, info ->
            //登出
            "退出登录--$code".showLog()
        }, {
            //token超时
            "token 超时".showLog()

        },{
            //token 即将超时
            "token 即将超时".showLog()
        })

        //登录
        HXIMHelper.getInstance().loginPsw("Aaron2024", "123456",
            {
                xBing?.leftIv?.post {

                    it.toast(activity as Context)
                }

                HXIMHelper.getInstance().flushAfterLogin()

            }, {code,tips ->

                xBing?.leftIv?.post {

                    "$code -- $tips".toast(activity as Context)
                }


            }, {progress, status ->

                xBing?.leftIv?.post {

                    "$progress -- $status".toast(activity as Context)
                }

            })

        return super.onCreateView(inflater, container, savedInstanceState)

    }

    override fun getBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): LayoutFragmentTab3Binding? = LayoutFragmentTab3Binding.inflate(inflater, container, false)

    override fun parseView(binding: LayoutFragmentTab3Binding?) {

        //测试按钮
        xBing?.leftIv?.setOnClickListener {

//            lifecycleScope.launch(Dispatchers.IO) {
//
//                //注册账号
////                HXIMHelper.getInstance().createAccountTest("Aaron2024",
////                    "123456", {
////
////                        if(TextUtils.isEmpty(it)) "创建账户失败".toast(activity as Context)
////                        else it!!.toast(activity as Context)
////
////                    })
//
//                val status = HXIMHelper.getInstance().logout()
//
//                withContext(Dispatchers.Main){
//
//                    "登出状态--$status".toast(activity as Context)
//                }
//
//            }

            //登录
//            HXIMHelper.getInstance().loginPsw("Aaron2024", "123456",
//                {
//                    xBing?.leftIv?.post {
//
//                        it.toast(activity as Context)
//                    }
//
//                }, {code,tips ->
//
//                    xBing?.leftIv?.post {
//
//                        "$code -- $tips".toast(activity as Context)
//                    }
//
//
//                }, {progress, status ->
//
//                    xBing?.leftIv?.post {
//
//                        "$progress -- $status".toast(activity as Context)
//                    }
//
//                })

            //发送一条消息
//            HXIMHelper.getInstance().sendSingleMsg("Aaron1987", "一天聊天测试消息", sendSuccess = {
//
//                xBing?.leftIv?.post {
//                    it.toast(activity as Context)
//                }
//
//
//            }, sendFailed = {code, tips ->
//
//                xBing?.leftIv?.post {
//                    "$code -- $tips".toast(activity as Context)
//                }
//
//            }, sendProgress = {progress, status ->
//
//                xBing?.leftIv?.post {
//                    "$progress -- $status".toast(activity as Context)
//                }
//
//            })

        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        HXIMHelper.getInstance().queryHistoryChatFormServer(querySuccess = {
//            activity?.runOnUiThread {
//
//                it?.let {
//
//                    val datas: List<EMMessage> = it.data
//
//                    if(null!=datas && !datas.isEmpty()){
//
//                        "聊天列表数据数量-${datas.size}".toast(activity as Context)
//                    }
//
//
//
//                } ?: "聊天列表数据为空-".toast(activity as Context)
//
//
//            }
//
//        }, queryFailed = {code, tips ->
//
//            activity?.runOnUiThread {
//                "聊天列表获取失败--$code---$tips".toast(activity as Context)
//            }
//        })

//        lifecycleScope.launch(Dispatchers.IO) {
//
//            val datas = HXIMHelper.getInstance().queryHistoryChatFormLoal("Aaron2024")
//
//            withContext(Dispatchers.Main){
//
//                if(null != datas && !datas.isEmpty()){
//
//                    "聊天列表数据数量-${datas.size}".toast(activity as Context)
//                } else {
//                    "聊天列表数据为空-".toast(activity as Context)
//                }
//            }
//
//        }

//        HXIMHelper.getInstance().queryConversationFormServer(querySuccess = {
//            it?.let {
//                val datas: List<EMConversation> = it.data
//
//                activity?.runOnUiThread {
//                    datas?.let {
//
//                        if(null!=it && !it.isEmpty()){
//
//                            "聊天会话列表数据数量-${it.size}".toast(activity as Context)
//                        }
//                    }
//
//                }?:"聊天会话列表数据为空-".toast(activity as Context)
//            }
//
//        }, queryFailed = {code, tips ->
//
//            activity?.runOnUiThread {
//                "聊天会话列表获取失败--$code---$tips".toast(activity as Context)
//            }
//        })

        xBing?.chatRV?.layoutManager = LinearLayoutManager(activity)
        xBing?.chatRV?.adapter = chatAdapter

        lifecycleScope.launch(Dispatchers.IO){

            val datas: List<EMConversation> = HXIMHelper.getInstance().queryConversationFormLocal()

            withContext(Dispatchers.Main){

                conversations.clear()
                conversations.addAll(datas)

                chatAdapter.notifyDataSetChanged()
            }

        }

    }


    override fun onDestroyView() {

        "onDestroyView 执行--".showLog()
        //设置接受消息监听
        HXIMHelper.getInstance().removeMessageListener()
        HXIMHelper.getInstance().removeConnectStatus()
        super.onDestroyView()
    }
}