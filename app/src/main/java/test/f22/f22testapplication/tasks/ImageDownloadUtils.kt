package test.f22.f22testapplication.tasks

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import test.f22.f22testapplication.base.F22Application

open class ImageDownloadUtils {

    companion object {
        fun downloadImage(dimensions : Int, url : String, view : ImageView){
//            val requestOptions = RequestOptions().fitCenter().override(dimensions, dimensions)
            Glide.with(F22Application.context).load(url).into(view)
        }
    }
}