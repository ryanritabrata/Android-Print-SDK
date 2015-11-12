/*****************************************************
 *
 * EditableImageContainerFrame.java
 *
 *
 * Modified MIT License
 *
 * Copyright (c) 2010-2015 Kite Tech Ltd. https://www.kite.ly
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The software MAY ONLY be used with the Kite Tech Ltd platform and MAY NOT be modified
 * to be used with any competitor platforms. This means the software MAY NOT be modified 
 * to place orders with any competitors to Kite Tech Ltd, all orders MUST go through the
 * Kite Tech Ltd platform servers. 
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 *****************************************************/

///// Package Declaration /////

package ly.kite.widget;


///// Import(s) /////

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import ly.kite.R;
import ly.kite.catalogue.Bleed;
import ly.kite.util.IImageConsumer;


///// Class Declaration /////

/*****************************************************
 *
 * This class is a container for an editable masked image.
 *
 *****************************************************/
public class EditableImageContainerFrame extends FrameLayout implements IImageConsumer
  {
  ////////// Static Constant(s) //////////

  @SuppressWarnings( "unused" )
  private static final String  LOG_TAG           = "EditableImageContain...";

  private static final boolean DEBUGGING_ENABLED = true;


  ////////// Static Variable(s) //////////


  ////////// Member Variable(s) //////////

  private EditableMaskedImageView  mEditableMaskedImageView;
  private ProgressBar              mProgressBar;

  private Object                   mImageKey;
  private Object                   mMaskKey;
  private Bleed                    mMaskBleed;

  private ICallback                mCallback;


  ////////// Static Initialiser(s) //////////


  ////////// Static Method(s) //////////


  ////////// Constructor(s) //////////

  public EditableImageContainerFrame( Context context )
    {
    super( context );

    initialise( context );
    }

  public EditableImageContainerFrame( Context context, AttributeSet attrs )
    {
    super( context, attrs );

    initialise( context );
    }

  public EditableImageContainerFrame( Context context, AttributeSet attrs, int defStyleAttr )
    {
    super( context, attrs, defStyleAttr );

    initialise( context );
    }

  @TargetApi( Build.VERSION_CODES.LOLLIPOP )
  public EditableImageContainerFrame( Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes )
    {
    super( context, attrs, defStyleAttr, defStyleRes );

    initialise( context );
    }


  ////////// IImageConsumer Method(s) //////////

  /*****************************************************
   *
   * Called when the remote image is being downloaded.
   *
   *****************************************************/
  @Override
  public void onImageDownloading( Object key )
    {
    if ( DEBUGGING_ENABLED ) Log.d( LOG_TAG, "onImageDownloading( key = " + key + " )" );

    if ( mProgressBar != null ) mProgressBar.setVisibility( View.VISIBLE );
    }


  /*****************************************************
   *
   * Called when the remote image has been loaded.
   *
   *****************************************************/
  @Override
  public void onImageAvailable( Object key, Bitmap bitmap )
    {
    if ( DEBUGGING_ENABLED ) Log.d( LOG_TAG, "onImageAvailable( key = " + key + ", bitmap = " + bitmap + " )" );

    if ( key == mImageKey ) mEditableMaskedImageView.setImageBitmap( bitmap );
    if ( key == mMaskKey  ) mEditableMaskedImageView.setMask( bitmap, mMaskBleed );


    // If we have everything we were expecting - remove the progress spinner

    if ( ( mImageKey == null || mEditableMaskedImageView.getImageBitmap()  != null ) &&
         ( mMaskKey  == null || mEditableMaskedImageView.getMaskDrawable() != null ) )
      {
      if ( mProgressBar != null ) mProgressBar.setVisibility( View.GONE );

      if ( mCallback != null ) mCallback.onImageAndMaskLoaded();
      }
    }


  /*****************************************************
   *
   * Called when the remote image could not be loaded.
   *
   *****************************************************/
  @Override
  public void onImageUnavailable( Object key, Exception exception )
    {
    if ( DEBUGGING_ENABLED ) Log.d( LOG_TAG, "onImageUnavailable( key = " + key + ", exception = " + exception + " )" );

    if ( mCallback != null ) mCallback.onImageLoadError( exception );
    }


  ////////// Method(s) //////////

  /*****************************************************
   *
   * Initialises the view.
   *
   *****************************************************/
  private void initialise( Context context )
    {
    LayoutInflater layoutInflater = LayoutInflater.from( context );

    View view = layoutInflater.inflate( R.layout.editable_image_container_frame, this, true );

    mEditableMaskedImageView = (EditableMaskedImageView)view.findViewById( R.id.editable_image_view );
    mProgressBar             = (ProgressBar)view.findViewById( R.id.progress_bar );
    }


  /*****************************************************
   *
   * Sets the callback.
   *
   *****************************************************/
  public void setCallback( ICallback callback )
    {
    mCallback = callback;
    }


  /*****************************************************
   *
   * Sets the key for the image request.
   *
   *****************************************************/
  public void setImageKey( Object key )
    {
    if ( DEBUGGING_ENABLED ) Log.d( LOG_TAG, "setImageKey( key = " + key + " )" );

    mImageKey = key;
    }


  /*****************************************************
   *
   * Clears the image and mask.
   *
   *****************************************************/
  public void clearAll()
    {
    if ( DEBUGGING_ENABLED ) Log.d( LOG_TAG, "clearAll()" );

    clearImage();
    clearMask();
    }


  /*****************************************************
   *
   * Clears the mask.
   *
   *****************************************************/
  public void clearMask()
    {
    if ( DEBUGGING_ENABLED ) Log.d( LOG_TAG, "clearMask()" );

    mEditableMaskedImageView.clearMask();
    }


  /*****************************************************
   *
   * Clears the image.
   *
   *****************************************************/
  public void clearImage()
    {
    if ( DEBUGGING_ENABLED ) Log.d( LOG_TAG, "clearImage()" );

    mEditableMaskedImageView.clearImage();
    }


  /*****************************************************
   *
   * Clears the current image and sets the key for a new
   * image.
   *
   *****************************************************/
  public void clearForNewImage( Object key )
    {
    if ( DEBUGGING_ENABLED ) Log.d( LOG_TAG, "clearForNewImage( key = " + key + " )" );

    clearImage();

    setImageKey( key );
    }


  /*****************************************************
   *
   * Sets the mask as a drawable resource.
   *
   *****************************************************/
  public void setMask( int resourceId, float aspectRatio )
    {
    if ( DEBUGGING_ENABLED ) Log.d( LOG_TAG, "setMask( resourceId = " + resourceId + ", aspectRatio = " + aspectRatio + " )" );

    mEditableMaskedImageView.setMask( resourceId, aspectRatio );
    }


  /*****************************************************
   *
   * Sets the request key and bleed for the mask.
   *
   *****************************************************/
  public void setMaskExtras( Object key, Bleed maskBleed )
    {
    if ( DEBUGGING_ENABLED ) Log.d( LOG_TAG, "setMaskExtras( key = " + key + ", maskBleed = " + maskBleed + " )" );

    mMaskKey   = key;
    mMaskBleed = maskBleed;
    }


  /*****************************************************
   *
   * Returns the masked image view.
   *
   *****************************************************/
  public EditableMaskedImageView getEditableImageView()
    {
    return ( mEditableMaskedImageView );
    }


  /*****************************************************
   *
   * Saves the state to a bundle. We only save the image
   * scale factor and position.
   *
   *****************************************************/
  public void saveState( Bundle outState )
    {
    if ( mEditableMaskedImageView != null ) mEditableMaskedImageView.saveState( outState );
    }


  /*****************************************************
   *
   * Restores the state to a bundle. We only try to restore
   * the image scale factor and position, and there is
   * no guarantee that they will be used.
   *
   *****************************************************/
  public void restoreState( Bundle inState )
    {
    if ( mEditableMaskedImageView != null ) mEditableMaskedImageView.restoreState( inState );
    }


  /*****************************************************
   *
   * Clears the state.
   *
   *****************************************************/
  public void clearState()
    {
    if ( mEditableMaskedImageView != null ) mEditableMaskedImageView.clearState();
    }


  ////////// Inner Class(es) //////////

  /*****************************************************
   *
   * A callback.
   *
   *****************************************************/
  public interface ICallback
    {
    public void onImageAndMaskLoaded();
    public void onImageLoadError( Exception exception );
    }

  }

