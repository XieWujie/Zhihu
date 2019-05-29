package com.example.administrator.zhihu


import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.administrator.zhihu.adapter.StoryAdapter
import com.example.administrator.zhihu.data.NetworkState
import com.example.administrator.zhihu.data.Ret
import com.example.administrator.zhihu.databinding.ActivityMainBinding
import com.example.administrator.zhihu.repository.NewsByNetRepository
import com.example.administrator.zhihu.ui.NewViewModel
import com.example.cache.lifecycle.AndroidLifecycleScopeProvide
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private lateinit var binding:ActivityMainBinding
    private lateinit var model: NewViewModel
    val provide = AndroidLifecycleScopeProvide()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_main)
        model = ViewModelProviders.of(this,object :ViewModelProvider.NewInstanceFactory(){
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                val repository = NewsByNetRepository(Ret.create(),Executors.newFixedThreadPool(5))
                return  NewViewModel(repository) as T
            }
        })[NewViewModel::class.java]
        binding.setLifecycleOwner(this)
        event()
        lifecycle.addObserver(provide)
        App.cacheProxy.registerLifecycleProvide(provide)
    }

    private fun event(){
        val adapter = StoryAdapter(){
            model.retry()
        }
        val recyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        model.posts.observe(this, Observer {
            adapter.submitList(it)
        } )
        model.networkState.observe(this, Observer {
           adapter.setNetworkState(it)
        })
        model.refreshState.observe(this, Observer {
            binding.fresh.isRefreshing = it == NetworkState.LOADING
        })
        binding.fresh.setOnRefreshListener {
            model.refresh()
        }
    }
}
