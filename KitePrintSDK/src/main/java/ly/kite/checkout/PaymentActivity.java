/*****************************************************
 *
 * PaymentActivity.java
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

package ly.kite.checkout;


///// Import(s) /////

import java.math.BigDecimal;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentConfirmation;
import com.paypal.android.sdk.payments.ProofOfPayment;

import io.card.payment.CardIOActivity;
import io.card.payment.CreditCard;

import ly.kite.analytics.Analytics;
import ly.kite.api.OrderState;
import ly.kite.pricing.OrderPricing;
import ly.kite.pricing.PricingAgent;
import ly.kite.KiteSDK;
import ly.kite.catalogue.MultipleCurrencyAmount;
import ly.kite.ordering.Order;
import ly.kite.R;
import ly.kite.payment.PayPalCard;
import ly.kite.payment.PayPalCardChargeListener;
import ly.kite.payment.PayPalCardVaultStorageListener;
import ly.kite.journey.AKiteActivity;
import ly.kite.catalogue.SingleCurrencyAmount;


///// Class Declaration /////

/*****************************************************
 *
 * This activity displays the price / payment screen.
 *
 *****************************************************/
public class PaymentActivity extends AOrderSubmissionActivity implements PricingAgent.IPricingConsumer,
                                                              TextView.OnEditorActionListener,
                                                              View.OnClickListener
  {
  ////////// Static Constant(s) //////////

  @SuppressWarnings("unused")
  private static final String LOG_TAG = "PaymentActivity";

  public static final String KEY_ORDER = "ly.kite.ORDER";


  ////////// Static Variable(s) //////////


  ////////// Member Variable(s) //////////

  private Order                mOrder;
  private String               mAPIKey;
  private KiteSDK.Environment  mKiteSDKEnvironment;

  private APaymentFragment     mPaymentFragment;

  private ListView             mOrderSummaryListView;
  private EditText             mPromoEditText;
  private Button               mPromoButton;
  private ProgressBar          mProgressBar;

  private OrderPricing         mOrderPricing;

  private boolean              mPromoActionClearsCode;
  private String               mLastSubmittedPromoCode;
  private boolean              mLastPriceRetrievalSucceeded;


  ////////// Static Initialiser(s) //////////


  ////////// Static Method(s) //////////

  /*****************************************************
   *
   * Convenience method for starting this activity.
   *
   *****************************************************/
  static public void startForResult( Activity activity, Order printOrder, int requestCode )
    {
    Intent intent = new Intent( activity, PaymentActivity.class );

    intent.putExtra( KEY_ORDER, printOrder );

    activity.startActivityForResult( intent, requestCode );
    }


  ////////// Constructor(s) //////////


  ////////// Activity Method(s) //////////

  /*****************************************************
   *
   * Called when the activity is created.
   *
   *****************************************************/
  @Override
  public void onCreate( Bundle savedInstanceState )
    {
    super.onCreate( savedInstanceState );


    // First look for a saved order (because it might have changed since we were first
    // created. If none if found - get it from the intent.

    if ( savedInstanceState != null )
      {
      mOrder = savedInstanceState.getParcelable( KEY_ORDER );
      }

    if ( mOrder == null )
      {
      Intent intent = getIntent();

      if ( intent != null )
        {
        mOrder = intent.getParcelableExtra( KEY_ORDER );
        }
      }

    if ( mOrder == null )
      {
      throw new IllegalArgumentException( "There must either be a saved Print Order, or one supplied in the intent used to start the Payment Activity" );
      }


    mKiteSDKEnvironment = KiteSDK.getInstance( this ).getEnvironment();


        /*
         * Start PayPal Service
         */

    PayPalConfiguration payPalConfiguration = new PayPalConfiguration()
            .clientId( mKiteSDKEnvironment.getPayPalClientId() )
            .environment( mKiteSDKEnvironment.getPayPalEnvironment() )
            .acceptCreditCards( false );

    Intent intent = new Intent( this, PayPalService.class );
    intent.putExtra( PayPalService.EXTRA_PAYPAL_CONFIGURATION, payPalConfiguration );

    startService( intent );


    // Set up the screen

    setContentView( R.layout.screen_payment );

    mOrderSummaryListView = (ListView)findViewById( R.id.order_summary_list_view );
    mPromoEditText        = (EditText)findViewById( R.id.promo_edit_text );
    mPromoButton          = (Button)findViewById( R.id.promo_button );
    mProgressBar          = (ProgressBar)findViewById( R.id.progress_bar );


    // Add the payment fragment

    if ( savedInstanceState == null )
      {
      ViewGroup paymentFragmentContainer = (ViewGroup)findViewById( R.id.payment_fragment_container );

      if ( paymentFragmentContainer != null )
        {
        KiteSDK kiteSDK = KiteSDK.getInstance( this );

        mPaymentFragment = kiteSDK.getPaymentFragment();

        getFragmentManager()
          .beginTransaction()
            .add( R.id.payment_fragment_container, mPaymentFragment, APaymentFragment.TAG )
          .commit();
        }
      }
    else
      {
      mPaymentFragment = (APaymentFragment)getFragmentManager().findFragmentByTag( APaymentFragment.TAG );
      }


    hideKeyboard();


    if ( mKiteSDKEnvironment.getPayPalEnvironment().equals( PayPalConfiguration.ENVIRONMENT_SANDBOX ) )
      {
      setTitle( R.string.title_payment_sandbox );
      }
    else
      {
      setTitle( R.string.title_payment );
      }


    Resources resources = getResources();


    // The prices are requested once the payment fragment has had its view created


    if ( savedInstanceState == null )
      {
      Analytics.getInstance( this ).trackPaymentScreenViewed( mOrder );
      }


    mPromoEditText.addTextChangedListener( new PromoCodeTextWatcher() );
    mPromoEditText.setOnEditorActionListener( this );
    }


  @Override
  public void onSaveInstanceState( Bundle outState )
    {
    super.onSaveInstanceState( outState );

    outState.putParcelable( KEY_ORDER, mOrder );
    }


  @Override
  protected void onActivityResult( int requestCode, int resultCode, Intent data )
    {
    // Pass any results to the payment fragment

    if ( mPaymentFragment != null )
      {
      mPaymentFragment.onActivityResult( requestCode, resultCode, data );
      }


    super.onActivityResult( requestCode, resultCode, data );
    }

  @Override
  public void onDestroy()
    {
    stopService( new Intent( this, PayPalService.class ) );
    super.onDestroy();
    }


  @Override
  public boolean onMenuItemSelected( int featureId, MenuItem item )
    {
    if ( item.getItemId() == android.R.id.home )
      {
      finish();
      return true;
      }
    return super.onMenuItemSelected( featureId, item );
    }


  ////////// IPricingConsumer Method(s) //////////

  /*****************************************************
   *
   * Called when the prices are successfully retrieved.
   *
   *****************************************************/
  @Override
  public void paOnSuccess( int requestId, OrderPricing pricing )
    {
    mOrderPricing                = pricing;

    mLastPriceRetrievalSucceeded = true;


    mPromoButton.setEnabled( true );

    if ( mPaymentFragment != null ) mPaymentFragment.onEnableButtons( true );

    mProgressBar.setVisibility( View.GONE );


    onGotPrices();
    }


  /*****************************************************
   *
   * Called when the prices could not be retrieved.
   *
   *****************************************************/
  @Override
  public void paOnError( int requestId, Exception exception )
    {
    mLastPriceRetrievalSucceeded = false;

    displayModalDialog
      (
      R.string.alert_dialog_title_oops,
      getString( R.string.alert_dialog_message_pricing_format_string, exception.getMessage() ),
      R.string.Retry,
      new RetrievePricingRunnable(),
      R.string.Cancel,
      new FinishRunnable()
      );
    }


  ////////// TextView.OnEditorActionListener Method(s) //////////

  /*****************************************************
   *
   * Called when an action occurs on the editor. We use this
   * to determine when the done button is pressed on the on-screen
   * keyboard.
   *
   *****************************************************/
  @Override
  public boolean onEditorAction( TextView v, int actionId, KeyEvent event )
    {
    if ( actionId == EditorInfo.IME_ACTION_DONE )
      {
      onPerformPromoAction();
      }

    // Return false even if we intercepted the done - so the keyboard
    // will be hidden.

    return ( false );
    }


  ////////// Method(s) //////////

  /*****************************************************
   *
   * Called by the payment fragment.
   *
   *****************************************************/
  public void onPaymentFragmentReady()
    {
    // Get the pricing information
    requestPrices();
    }


  /*****************************************************
   *
   * Requests pricing information.
   *
   *****************************************************/
  void requestPrices()
    {
    mLastSubmittedPromoCode = mPromoEditText.getText().toString();

    if ( mLastSubmittedPromoCode.trim().equals( "" ) ) mLastSubmittedPromoCode = null;

    mOrderPricing  = PricingAgent.getInstance().requestPricing( this, mOrder, mLastSubmittedPromoCode, this );


    // If the pricing wasn't cached - disable the buttons, and show the progress spinner, whilst
    // they are retrieved.

    if ( mOrderPricing == null )
      {
      mPromoButton.setEnabled( false );

      if ( mPaymentFragment != null ) mPaymentFragment.onEnableButtons( false );

      mProgressBar.setVisibility( View.VISIBLE );

      return;
      }


    onGotPrices();
    }


  /*****************************************************
   *
   * Updates the screen once we have retrieved the pricing
   * information.
   *
   *****************************************************/
  void onGotPrices()
    {
    if ( mPaymentFragment != null ) mPaymentFragment.onOrderPricing( mOrderPricing );


    // Verify that amy promo code was accepted

    String promoCodeInvalidMessage = mOrderPricing.getPromoCodeInvalidMessage();

    if ( promoCodeInvalidMessage != null )
      {
      // A promo code was sent with the request but was invalid.

      // Change the colour to highlight it
      mPromoEditText.setEnabled( true );
      mPromoEditText.setTextColor( getResources().getColor( R.color.payment_promo_code_text_error ) );

      mPromoButton.setText( R.string.payment_promo_button_text_clear );

      mPromoActionClearsCode = true;


      // Note that we show an error message, but we still update the
      // order summary and leave the buttons enabled. That way the
      // user can still pay without the benefit of any promotional
      // discount.

      showErrorDialog( promoCodeInvalidMessage );
      }
    else
      {
      // Either there was no promo code, or it was valid. Save which ever it was.

      mOrder.setPromoCode( mLastSubmittedPromoCode );


      // If there is a promo code - change the text to "Clear" immediately following a retrieval. It
      // will get changed back to "Apply" as soon as the field is changed.

      if ( setPromoButtonEnabledState() )
        {
        mPromoEditText.setEnabled( false );

        mPromoButton.setText( R.string.payment_promo_button_text_clear );

        mPromoActionClearsCode = true;
        }
      else
        {
        mPromoEditText.setEnabled( true );

        mPromoButton.setText( R.string.payment_promo_button_text_apply );

        mPromoActionClearsCode = false;
        }
      }


    // Get the total cost, and save it in the order

    MultipleCurrencyAmount totalCost = mOrderPricing.getTotalCost();

    mOrder.setOrderPricing( mOrderPricing );


    // If the cost is zero, we change the button text
    if ( totalCost.getDefaultAmountWithFallback().getAmount().compareTo( BigDecimal.ZERO ) <= 0 )
      {
      if ( mPaymentFragment != null ) mPaymentFragment.onCheckoutFree( true );
      }
    else
      {
      if ( mPaymentFragment != null ) mPaymentFragment.onCheckoutFree( false );
      }


    OrderPricingAdaptor adaptor = new OrderPricingAdaptor( this, mOrderPricing );

    mOrderSummaryListView.setAdapter( adaptor );
    }


  /*****************************************************
   *
   * Sets the enabled state of the promo button.
   *
   * @return The enabled state.
   *
   *****************************************************/
  private boolean setPromoButtonEnabledState()
    {
    boolean isEnabled = ( mPromoEditText.getText().length() > 0 );

    mPromoButton.setEnabled( isEnabled );

    return ( isEnabled );
    }


  /*****************************************************
   *
   * Called when the promo button is called. It may be
   * in one of two states:
   *   - Apply
   *   - Clear
   *
   *****************************************************/
  public void onPromoButtonClicked( View view )
    {
    onPerformPromoAction();
    }


  /*****************************************************
   *
   * Called when the promo button is called. It may be
   * in one of two states:
   *   - Apply
   *   - Clear
   *
   *****************************************************/
  public void onPerformPromoAction()
    {
    if ( mPromoActionClearsCode )
      {
      mPromoEditText.setEnabled( true );
      mPromoEditText.setText( null );

      mPromoButton.setText( R.string.payment_promo_button_text_apply );
      mPromoButton.setEnabled( false );

      mPromoActionClearsCode = false;


      // If we are clearing a promo code that was successfully used - re-request the
      // prices (i.e. without the code).

      if ( mLastSubmittedPromoCode != null && mLastPriceRetrievalSucceeded )
        {
        requestPrices();
        }
      }
    else
      {
      hideKeyboardDelayed();

      requestPrices();
      }
    }


  /*****************************************************
   *
   * Submits the order for printing.
   *
   *****************************************************/
  public void submitOrderForPrinting( String proofOfPayment, String analyticsPaymentMethod )
    {
    if ( proofOfPayment != null )
      {
      mOrder.setProofOfPayment( proofOfPayment );
      }

    Analytics.getInstance( this ).trackPaymentCompleted( mOrder, analyticsPaymentMethod );

    if ( mPaymentFragment != null ) mPaymentFragment.onPreSubmission( mOrder );

    submitOrder( mOrder );
    }


  ////////// Inner Class(es) //////////

  /*****************************************************
   *
   * A text watcher for the promo code.
   *
   *****************************************************/
  private class PromoCodeTextWatcher implements TextWatcher
    {
    @Override
    public void beforeTextChanged( CharSequence charSequence, int i, int i2, int i3 )
      {
      // Ignore
      }

    @Override
    public void onTextChanged( CharSequence charSequence, int i, int i2, int i3 )
      {
      // Ignore
      }

    @Override
    public void afterTextChanged( Editable editable )
      {
      // Clear any error colour on the text
      mPromoEditText.setTextColor( getResources().getColor( R.color.payment_promo_code_text_default ) );

      // Set the enabled state
      setPromoButtonEnabledState();

      // Change the button text back to Apply (even if we disable the button because the code is blank)
      mPromoButton.setText( R.string.payment_promo_button_text_apply );

      mPromoActionClearsCode = false;
      }
    }


  /*****************************************************
   *
   * Starts pricing retrieval.
   *
   *****************************************************/
  private class RetrievePricingRunnable implements Runnable
    {
    @Override
    public void run()
      {
      requestPrices();
      }
    }


  }
