package com.pujh.router

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.pujh.router.annotations.Destination
import com.pujh.router.annotations.Listed

@Destination("route://home", "主页")
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}

// 可以位于项目中的任何位置（或任何模块中）
@Listed("mainList")
fun mainModule() = 2

// 可以位于项目中的任何位置（或任何模块中）
@Listed("otherList")
fun helloModule() = "hello!"

// 可以位于项目中的任何位置（或任何模块中）
@Listed("mainList")
fun secondModule() = 3