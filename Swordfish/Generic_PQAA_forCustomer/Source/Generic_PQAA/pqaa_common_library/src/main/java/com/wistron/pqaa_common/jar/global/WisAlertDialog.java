package com.wistron.pqaa_common.jar.global;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnShowListener;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class WisAlertDialog extends AlertDialog.Builder {
	private float messageSize = 40,positiveButtonSize = 50,negativeButtonSize = 50,neutralButtonSize = 50;
	private AlertDialog mDialog;
	
	public WisAlertDialog(Context context) {
		super(context);
		super.setCancelable(false);
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * Set the title displayed in the Dialog.
	 * @param title
	 * the title of Dialog
	 * @return
	 * This Builder object to allow for chaining of calls to set methods
	 */
	public WisAlertDialog setTitle(String title){
		super.setTitle(title);
		if (mDialog != null) {
			mDialog.setTitle(title);
		}
		return this;
	}
	
	/**
	 * Set the message to display.
	 * @param msg
	 * the message to display
	 * @return
	 * This Builder object to allow for chaining of calls to set methods
	 */
	public WisAlertDialog setMessage(String msg){
		super.setMessage(msg);
		if (mDialog != null) {
			mDialog.setMessage(msg);
		}
		return this;
	}
	
	/**
	 * Set the message to display.
	 * @param msg
	 * the message to display
	 * @param size
	 * the font size of dialog message
	 * @return
	 * This Builder object to allow for chaining of calls to set methods
	 */
	public WisAlertDialog setMessage(String msg,float size){
		this.messageSize=size;
		return this.setMessage(msg);
	}
	
	public WisAlertDialog setView(View view){
		super.setView(view);
		if (mDialog != null) {
			mDialog.setView(view);
		}
		return this;
	}
	
	/**
	 * Set a listener to be invoked when the positive button of the dialog is pressed.
	 * @param text
	 * The text to display in the positive button 
	 * @param listener
	 * The DialogInterface.OnClickListener to use.
	 * @return
	 * This Builder object to allow for chaining of calls to set methods
	 */
	public WisAlertDialog setPositiveButton(String text,OnClickListener listener){
		super.setPositiveButton(text, listener);
		if (mDialog != null) {
			mDialog.setButton(AlertDialog.BUTTON_POSITIVE, text, listener);
		}
		return this;
	}
	
	/**
	 * Set a listener to be invoked when the positive button of the dialog is pressed.
	 * @param text
	 * The text to display in the positive button 
	 * @param size
	 * The text size of button
	 * @param listener
	 * The DialogInterface.OnClickListener to use.
	 * @return
	 * This Builder object to allow for chaining of calls to set methods
	 */
	public WisAlertDialog setPositiveButton(String text,float size,OnClickListener listener){
		this.positiveButtonSize = size;
		return this.setPositiveButton(text, listener);
	}
	
	/**
	 * Set a listener to be invoked when the negative button of the dialog is pressed.
	 * @param text
	 * The text to display in the negative button
	 * @param listener
	 * The DialogInterface.OnClickListener to use.
	 * @return
	 * This Builder object to allow for chaining of calls to set methods
	 */
	public WisAlertDialog setNegativeButton(String text,OnClickListener listener){
		super.setNegativeButton(text, listener);
		if (mDialog != null) {
			mDialog.setButton(AlertDialog.BUTTON_NEGATIVE, text, listener);
		}
		return this;
	}
	
	/**
	 * Set a listener to be invoked when the negative button of the dialog is pressed.
	 * @param text
	 * The text to display in the negative button
	 * @param size
	 * The text size of button
	 * @param listener
	 * @return
	 * This Builder object to allow for chaining of calls to set methods
	 */
	public WisAlertDialog setNegativeButton(String text,float size,OnClickListener listener){
		this.negativeButtonSize = size;
		return this.setNegativeButton(text, listener);
	}
	
	/**
	 * Set a listener to be invoked when the neutral button of the dialog is pressed.
	 * @param text
	 * The text to display in the neutral button
	 * @param listener
	 * The DialogInterface.OnClickListener to use.
	 * @return
	 * This Builder object to allow for chaining of calls to set methods
	 */
	public WisAlertDialog setNeutralButton(String text,OnClickListener listener){
		super.setNeutralButton(text, listener);
		if (mDialog != null) {
			mDialog.setButton(AlertDialog.BUTTON_NEUTRAL, text, listener);
		}
		return this;
	}
	
	/**
	 * Set a listener to be invoked when the neutral button of the dialog is pressed.
	 * @param text
	 * The text to display in the neutral button
	 * @param size
	 * The text size of button
	 * @param listener
	 * The DialogInterface.OnClickListener to use.
	 * @return
	 * This Builder object to allow for chaining of calls to set methods
	 */
	public WisAlertDialog setNeutralButton(String text,float size,OnClickListener listener){
		this.neutralButtonSize = size;
		return this.setNeutralButton(text, listener);
	}
	
	/* (non-Javadoc)
	 * Sets whether the dialog is cancelable or not. Default is true.
	 * @see android.app.AlertDialog.Builder#setCancelable(boolean)
	 */
	public WisAlertDialog setCancelable(boolean cancelable){
		super.setCancelable(cancelable);
		if (mDialog != null) {
			mDialog.setCancelable(cancelable);
		}
		return this;
	}
	
	/**
	 * Creates a AlertDialog with the arguments supplied to this builder. 
	 * It does not show() the dialog. This allows the user to do any extra processing 
	 * before displaying the dialog. Use show() if you don't have any other processing to 
	 * do and want this to be created and displayed.
	 * @return
	 * This Builder object to allow for chaining of calls to set methods
	 */
	public WisAlertDialog createDialog(){
		mDialog = super.create();
		mDialog.setOnShowListener(new OnShowListener() {
			
			@Override
			public void onShow(DialogInterface dialog) {
				// TODO Auto-generated method stub
				if (messageSize > 0) {
					TextView messageView=((TextView)((AlertDialog)dialog).findViewById(android.R.id.message));
					if (messageView != null) {
						messageView.setTextSize(messageSize);
					}
				}
				if (positiveButtonSize > 0) {
					Button positiveButton=((Button)((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE));
					if (positiveButton != null) {
						positiveButton.setTextSize(positiveButtonSize);
					}
				}
				if (negativeButtonSize > 0) {
					Button negativeButton = ((Button)((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE));
					if (negativeButton != null) {
						negativeButton.setTextSize(negativeButtonSize);
					}
				}
				if (neutralButtonSize > 0) {
					Button neutralButton = ((Button)((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEUTRAL));
					if (neutralButton != null) {
						neutralButton.setTextSize(neutralButtonSize);
					}
				}
			}
		});
		return this;
	}
	
	/**
	 * Start the dialog and display it on screen. 
	 */
	public void showDialog(){
		if (mDialog == null) {
			this.createDialog();
		}
		if (mDialog != null) {
			mDialog.show();
		}
	}
	
	/**
	 * Dismiss this dialog, removing it from the screen. 
	 */
	public void dismissDialog(){
		if (mDialog !=null && mDialog.isShowing()) {
			mDialog.dismiss();
		}
	}
	
	/**
	 * Whether the dialog is currently showing.
	 * @return
	 * Whether the dialog is currently showing.
	 */
	public boolean isShowing() {
		if (mDialog != null) {
			return mDialog.isShowing();
		}else {
			return true;
		}
	}

}
