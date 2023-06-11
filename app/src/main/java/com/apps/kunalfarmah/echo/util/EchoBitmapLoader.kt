package com.apps.kunalfarmah.echo.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.media3.common.MediaMetadata
import androidx.media3.session.BitmapLoader
import androidx.media3.session.SimpleBitmapLoader
import com.apps.kunalfarmah.echo.App
import com.apps.kunalfarmah.echo.R
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import java.util.Arrays


class EchoBitmapLoader() : BitmapLoader {
    private var bitmapLoader = SimpleBitmapLoader()

    private var lastBitmapLoadRequest: BitmapLoadRequest? = null

    /**
     * Stores the result of a bitmap load request. Requests are identified either by a byte array, if
     * the bitmap is loaded from compressed data, or a URI, if the bitmap was loaded from a URI.
     */
    private class BitmapLoadRequest {
        private val data: ByteArray?
        private val uri: Uri?
        private val future: ListenableFuture<Bitmap>

        constructor(data: ByteArray?, future: ListenableFuture<Bitmap>) {
            this.data = data
            uri = null
            this.future = future
        }

        constructor(uri: Uri?, future: ListenableFuture<Bitmap>) {
            data = null
            this.uri = uri
            this.future = future
        }

        /** Whether the bitmap load request was performed for `data`.  */
        fun matches(data: ByteArray?): Boolean {
            return this.data != null && Arrays.equals(this.data, data)
        }

        /** Whether the bitmap load request was performed for `uri`.  */
        fun matches(uri: Uri?): Boolean {
            return this.uri != null && this.uri == uri
        }

        /** Returns the future that set for the bitmap load request.  */
        fun getFuture(): ListenableFuture<Bitmap> {
            return future
        }
    }

    override fun decodeBitmap(data: ByteArray): ListenableFuture<Bitmap> {
        if (lastBitmapLoadRequest != null && lastBitmapLoadRequest!!.matches(data)) {
            return lastBitmapLoadRequest!!.getFuture()
        }
        val future: ListenableFuture<Bitmap> = bitmapLoader!!.decodeBitmap(data)
        lastBitmapLoadRequest = BitmapLoadRequest(data, future)
        return future
    }

    override fun loadBitmap(uri: Uri): ListenableFuture<Bitmap> {
        if (lastBitmapLoadRequest != null && lastBitmapLoadRequest!!.matches(uri)) {
            return lastBitmapLoadRequest!!.getFuture()
        }
        val future: ListenableFuture<Bitmap> = bitmapLoader!!.loadBitmap(uri)
        lastBitmapLoadRequest = BitmapLoadRequest(uri, future)
        return future
    }

    override fun loadBitmapFromMetadata(metadata: MediaMetadata): ListenableFuture<Bitmap>? {
        val future: ListenableFuture<Bitmap>?
        future = if (metadata.artworkData != null) {
            decodeBitmap(metadata.artworkData!!)
        } else if (metadata.artworkUri != null) {
            loadBitmap(metadata.artworkUri!!)
        } else {
            val bitmap = BitmapFactory.decodeResource(App.context.resources, R.drawable.echo_icon)
            Futures.immediateFuture(bitmap)
        }
        return future
    }

}