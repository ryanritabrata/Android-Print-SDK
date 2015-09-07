/*****************************************************
 *
 * PhoneCaseFragment.java
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

package ly.kite.journey.phonecase;


///// Import(s) /////

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.net.URL;
import java.util.ArrayList;

import ly.kite.R;
import ly.kite.journey.AEditImageFragment;
import ly.kite.journey.AKiteActivity;
import ly.kite.product.Asset;
import ly.kite.journey.AssetsAndQuantity;
import ly.kite.product.AssetHelper;
import ly.kite.product.Bleed;
import ly.kite.product.Product;
import ly.kite.util.ImageAgent;


///// Class Declaration /////

/*****************************************************
 *
 * This activity allows the user to create a phone
 * case design using an image.
 *
 *****************************************************/
public class PhoneCaseFragment extends AEditImageFragment
  {
  ////////// Static Constant(s) //////////

  @SuppressWarnings( "unused" )
  private static final String      LOG_TAG                            = "PhoneCaseFragment";


  ////////// Static Variable(s) //////////


  ////////// Member Variable(s) //////////


  ////////// Static Initialiser(s) //////////


  ////////// Static Method(s) //////////

  /*****************************************************
   *
   * Creates a new instance of this fragment.
   *
   *****************************************************/
  public static PhoneCaseFragment newInstance( Product product )
    {
    PhoneCaseFragment fragment = new PhoneCaseFragment();

    Bundle arguments = new Bundle();
    arguments.putParcelable( BUNDLE_KEY_PRODUCT, product );

    fragment.setArguments( arguments );

    return ( fragment );
    }


  ////////// Constructor(s) //////////


  ////////// AEditImageFragment Method(s) //////////

  /*****************************************************
   *
   * Called when the activity is created.
   *
   *****************************************************/
  @Override
  public void onCreate( Bundle savedInstanceState )
    {
    super.onCreate( savedInstanceState );


    // TODO: Uncomment when image selection is implemented
    //this.setHasOptionsMenu( true );
    }


  /*****************************************************
   *
   * Called the first time the options menu is created.
   *
   *****************************************************/
// TODO: Uncomment once we implement the add photo functionality
//  @Override
//  public boolean onCreateOptionsMenu( Menu menu )
//    {
//    MenuInflater menuInflator = getMenuInflater();
//
//    menuInflator.inflate( R.menu.add_photo, menu );
//
//    return ( true );
//    }


  /*****************************************************
   *
   * Returns the content view for this fragment
   *
   *****************************************************/
  @Override
  public View onCreateView( LayoutInflater layoutInflator, ViewGroup container, Bundle savedInstanceState )
    {
    View view = super.onCreateView( layoutInflator, container, savedInstanceState );


    mCancelButton.setVisibility( View.GONE );

    mConfirmButton.setVisibility( View.VISIBLE );
    mConfirmButton.setText( R.string.product_creation_next_button_text );


    return ( view );
    }


  /*****************************************************
   *
   * Called after the activity is created.
   *
   *****************************************************/
  @Override
  public void onActivityCreated( Bundle savedInstanceState )
    {
    super.onActivityCreated( savedInstanceState );


    // Request the image and mask

    ImageAgent imageManager = ImageAgent.getInstance( mKiteActivity );

    Asset        asset        = mAssetsAndQuantityArrayList.get( 0 ).getUneditedAsset();
    URL          maskURL      = mProduct.getMaskURL();
    Bleed        maskBleed    = mProduct.getMaskBleed();

    mEditableConsumerImageView.setImageKey( asset );
    mEditableConsumerImageView.setMaskExtras( maskURL, maskBleed );

    AssetHelper.requestImage( mKiteActivity, asset, mEditableConsumerImageView );
    imageManager.requestImage( AKiteActivity.IMAGE_CLASS_STRING_PRODUCT_ITEM, maskURL, mEditableConsumerImageView );
    }


  /*****************************************************
   *
   * Called when an item in the options menu is selected.
   *
   *****************************************************/
  @Override
  public boolean onOptionsItemSelected( MenuItem item )
    {
    // See what menu item was selected

    int itemId = item.getItemId();

    if ( itemId == R.id.add_photo )
      {
      ///// Add photo /////

      // TODO:

      return ( true );
      }


    return ( super.onOptionsItemSelected( item ) );
    }


  /*****************************************************
   *
   * Called when the fragment is top-most.
   *
   *****************************************************/
  @Override
  public void onTop()
    {
    super.onTop();

    if ( mProduct != null ) mKiteActivity.setTitle( mProduct.getName() );
    }


  ////////// Method(s) //////////

  /*****************************************************
   *
   * Called when the Next button is clicked.
   *
   *****************************************************/
  @Override
  protected void onConfirm()
    {
    Asset editedImageAsset = getEditedImageAsset();

    if ( editedImageAsset == null ) return;


    // Call back to the activity
    if ( mKiteActivity instanceof ICallback )
      {
      ( (ICallback)mKiteActivity ).pcOnCreated( editedImageAsset );
      }
    }


  ////////// Inner Class(es) //////////

  /*****************************************************
   *
   * A callback interface.
   *
   *****************************************************/
  public interface ICallback
    {
    public void pcOnCreated( Asset imageAsset );
    }

  }

