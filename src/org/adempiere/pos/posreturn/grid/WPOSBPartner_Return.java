/******************************************************************************
 * Product: Posterita Ajax UI 												  *
 * Copyright (C) 2007 Posterita Ltd.  All Rights Reserved.                    *
 * This program is free software; you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program; if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 * For the text or an alternative of this public license, you may reach us    *
 * Posterita Ltd., 3, Draper Avenue, Quatre Bornes, Mauritius                 *
 * or via info@posterita.org or http://www.posterita.org/                     *
 *****************************************************************************/
package org.adempiere.pos.posreturn.grid;

import java.util.logging.Level;

import org.adempiere.pos.WPOS;
//import org.adempiere.pos.WPOSKeyboard;
import org.adempiere.pos.WPOSTextField;
import org.adempiere.pos.posreturn.WPOS_Return;
//import org.adempiere.webui.AdempiereWebUI;
//import org.adempiere.webui.apps.AEnv;
import org.adempiere.webui.component.ConfirmPanel;
import org.adempiere.webui.component.Label;
import org.adempiere.webui.component.Textbox;
import org.adempiere.webui.component.VerticalBox;
//import org.adempiere.webui.component.WTaxIdPopup;
import org.adempiere.webui.component.Window;
import org.adempiere.webui.editor.WLocationEditor;
import org.adempiere.webui.util.ZKUpdateUtil;
//import org.adempiere.webui.event.DialogEvents;
//import org.adempiere.webui.theme.ThemeManager;
import org.adempiere.webui.window.FDialog;
//import org.compiere.model.GridField;
import org.compiere.model.GridTab;
import org.compiere.model.GridWindow;
import org.compiere.model.MBPartner;
import org.compiere.model.MBPartnerLocation;
import org.compiere.model.MBPartnerPOS;
import org.compiere.model.MLocation;
import org.compiere.model.MLocationLookup;
import org.compiere.model.MRole;
import org.compiere.model.MUser;
import org.compiere.model.MWindow;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.util.Msg;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Hbox;
import org.zkoss.zul.Separator;

//import it.cnet.impl.editorNatIDNumber.editor.TaxIDbox;

/**
 * Business Partner : Based on VBPartner
 *
 * @author 	Niraj Sohun
 * 			Aug 15, 2007
 * @author Mario Calderon, mario.calderon@westfalia-it.com, Systemhaus Westfalia, http://www.westfalia-it.com
 * @author Raul Muñoz, rmunoz@erpcya.com, ERPCYA http://www.erpcya.com
 * @author Yamel Senih, ysenih@erpcya.com, ERPCyA http://www.erpcya.com
 */

public class WPOSBPartner_Return extends Window implements EventListener<Event>
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 5842369060073088746L;
	
	/**
	 *	Constructor.
	 *	Requires call loadBPartner
	 * 	@param frame	parent
	 * 	@param WindowNo	Window No
	 */
	public WPOSBPartner_Return(int WindowNo, WPOS_Return pos) {
		super();
		this.pos = pos;
		m_WindowNo = WindowNo;
		m_readOnly = !MRole.getDefault().canUpdate(
			Env.getAD_Client_ID(Env.getCtx()), Env.getAD_Org_ID(Env.getCtx()), 
			MBPartner.Table_ID, 0, false);
		log.info("R/O=" + m_readOnly);
		
		try
		{
			jbInit();
		}
		catch(Exception ex)
		{
			log.log(Level.SEVERE, ex.getMessage());
		}
		
		initBPartner();
		
	}	//	WBPartner


	
	private static CLogger log = CLogger.getCLogger(WPOSBPartner_Return.class);
	
	private int 				m_WindowNo;
	
	/** The Partner				*/
	private MBPartner 			partner = null;
	
	/** The Location			*/
	private MBPartnerLocation 	m_pLocation = null;
	
	/** The User				*/
	private MUser 				m_user = null;
	
	/** Read Only				*/
	private boolean 			m_readOnly = false;

	private boolean		 		isKeyboard;
	private WPOSTextField 		fValue = new WPOSTextField(null, null);
	private WPOSTextField 		fName = new WPOSTextField(null, null);
	private WPOSTextField 		fName2 = new WPOSTextField(null, null);
	private WPOSTextField 		fContact = new WPOSTextField(null, null);
	private WPOSTextField 		fEMail = new WPOSTextField(null, null);
	private WPOSTextField 		fPhone = new WPOSTextField(null, null);
	private WPOSTextField 		fPhone2 = new WPOSTextField(null, null);
	
	private WLocationEditor 	fAddress;/* = new WLocationDialog();*/
	
	private VerticalBox 		centerPanel = new VerticalBox();
	
	private ConfirmPanel 		confirmPanel = new ConfirmPanel(true, false, false, false, false, false);
	
	private WPOS_Return		pos = null;
	
	//private TaxIDbox  taxIdNumb;

	/**
	 *	Static Init
	 * 	@throws Exception
	 */
	void jbInit() throws Exception
	{
		this.setWidth("350px");
		this.setBorder("normal");
		this.setClosable(true);
		this.setTitle("Business Partner");
		this.setAttribute("mode", Window.MODE_HIGHLIGHTED);
		this.appendChild(centerPanel);
		this.appendChild(confirmPanel);
		
		confirmPanel.addActionListener(Events.ON_CLICK, this);
	}
	
	/**
	 *	Dynamic Init
	 */
	private void initBPartner()
	{
		//	Value
		fValue.addEventListener(this);
		fValue.setWidth("97%");
		fValue.setStyle(WPOS.FONTSIZESMALL);
		createLine (fValue, "Value", true);
		
		//	Name
		fName.addEventListener(this);
		fName.setWidth("97%");
		fName.setStyle(WPOS.FONTSIZESMALL);
		createLine (fName, "Name", false)/*.setFontBold(true)*/;

		//	Name2
		fName2.addEventListener(this);
		fName2.setWidth("97%");
		fName2.setStyle(WPOS.FONTSIZESMALL);
		createLine (fName2, "Name2", false);
		
		//	Contact
		fContact.addEventListener(this);
		fContact.setWidth("97%");
		fContact.setStyle(WPOS.FONTSIZESMALL);
		createLine (fContact, "Contact", true)/*.setFontBold(true)*/;

		//	Email
		fEMail.addEventListener(this);
		fEMail.setWidth("97%");
		fEMail.setStyle(WPOS.FONTSIZESMALL);
		createLine (fEMail, "EMail", false);
		
		//	Location
		boolean ro = m_readOnly;
		
		if (!ro)
			ro = !MRole.getDefault().canUpdate(
				Env.getAD_Client_ID(Env.getCtx()), Env.getAD_Org_ID(Env.getCtx()), 
				MBPartnerLocation.Table_ID, 0, false);
		
		if (!ro)
			ro = !MRole.getDefault().canUpdate(
				Env.getAD_Client_ID(Env.getCtx()), Env.getAD_Org_ID(Env.getCtx()), 
				MLocation.Table_ID, 0, false);
		
		fAddress = new WLocationEditor("C_Location_ID", false, ro, true, 
				new MLocationLookup (Env.getCtx(), m_WindowNo));
//		fAddress.addValueChangeListener((org.adempiere.webui.event.ValueChangeListener) this);
		fAddress.setValue (null);
		createLine (fAddress.getComponent(), "C_Location_ID", true)/*.setFontBold(true)*/;
		
		//	Phone
		fPhone.addEventListener(this);
		fPhone.setWidth("97%");
		fPhone.setStyle(WPOS.FONTSIZESMALL);
		createLine (fPhone, "Phone", true);
		
		//	Phone2
		fPhone2.addEventListener(this);
		fPhone2.setWidth("97%");
		fPhone2.setStyle(WPOS.FONTSIZESMALL);
		createLine (fPhone2, "Phone2", false);
		
		//p.iva
		GridWindow gridWindow = GridWindow.get(Env.getCtx(), 0, MWindow.getWindow_ID("Business Partner"), true);
		GridTab gridTab = gridWindow.getTab(0);
		gridTab.initTab(false);
//		GridField gridField = gridTab.getField("LIT_TaxID");
		
//		taxIdNumb = new TaxIDbox();
//		taxIdNumb.setEnabled(true);
//		taxIdNumb.getTextbox().setReadonly(false);
//		taxIdNumb.setButtonImage(ThemeManager.getThemeResource("images/Location10.png"));
////		taxIdNumb.getComponent().setWidth("97%");
//		taxIdNumb.getTextbox().setStyle(WPOS.FONTSIZESMALL);
//		createLine(taxIdNumb, "LIT_TaxID", false);
//		taxIdNumb.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
//
//			@Override
//			public void onEvent(Event event) throws Exception {
//				if(taxIdNumb.getText()!=null && taxIdNumb.getText().trim().length()>0){
//					final WTaxIdPopup window = new WTaxIdPopup((String)taxIdNumb.getText());
//					window.setWidgetAttribute(AdempiereWebUI.WIDGET_INSTANCE_NAME, "taxIDNumber");
//					window.setAttribute(Window.MODE_KEY, Window.MODE_HIGHLIGHTED);
//					window.addEventListener(DialogEvents.ON_WINDOW_CLOSE, new EventListener<Event>() {
//						@Override
//						public void onEvent(Event event) throws Exception {
//							if(window.getSuccessOK()){
//								
//								pos.configureBPartner(window.getBP_ID());
//								//	Refresh
//								pos.refreshPanel();
//								
//								dispose();
//								
//	//							setValue(window.getValueTaxID());
//	//							ValueChangeEvent vc = new ValueChangeEvent(WTaxIdNumbEditor.this,getColumnName(),null,window.getValueTaxID());
//	//							fireValueChange(vc);
//							}
//						}
//					});
//					AEnv.showWindow(window);
//				}
//			}
//		});
//		taxIdNumb.addEventListener(Events.ON_OK, new EventListener<Event>() {
//
//			@Override
//			public void onEvent(Event event) throws Exception {
//				if(taxIdNumb.getText()!=null && taxIdNumb.getText().trim().length()>0){
//					final WTaxIdPopup window = new WTaxIdPopup((String)taxIdNumb.getText());
//					window.setWidgetAttribute(AdempiereWebUI.WIDGET_INSTANCE_NAME, "taxIDNumber");
//					window.setAttribute(Window.MODE_KEY, Window.MODE_HIGHLIGHTED);
//					window.addEventListener(DialogEvents.ON_WINDOW_CLOSE, new EventListener<Event>() {
//						@Override
//						public void onEvent(Event event) throws Exception {
//							if(window.getSuccessOK()){
//								
//								pos.configureBPartner(window.getBP_ID());
//								//	Refresh
//								pos.refreshPanel();
//								
//								dispose();
//								
//	//							setValue(window.getValueTaxID());
//	//							ValueChangeEvent vc = new ValueChangeEvent(WTaxIdNumbEditor.this,getColumnName(),null,window.getValueTaxID());
//	//							fireValueChange(vc);
//							}
//						}
//					});
//					AEnv.showWindow(window);
//				}
//			}
//		});
		
		
		//
		
	}	//	initBPartner

	/**
	 * 	Create Line
	 * 	@param field 	field
	 * 	@param title	label value
	 * 	@param addSpace	add more space
	 * 	@return label
	 */
	
	private Label createLine (Component field, String title, boolean addSpace)
	{
		Hbox hbox = new Hbox(); 
		
		hbox.setWidth("100%");
		ZKUpdateUtil.setWidth(hbox, "30%, 70%");
		
		Label label = new Label(Msg.translate(Env.getCtx(), title));
		label.setStyle(WPOS.FONTSIZESMALL);
		hbox.appendChild(label.rightAlign());
		hbox.appendChild(field);
		
		centerPanel.appendChild(hbox);
		centerPanel.appendChild(new Separator());
		
		return label;
	}	//	createLine

	/**
	 *	Load BPartner
	 *  @param C_BPartner_ID - existing BPartner or 0 for new
	 * 	@return true if loaded
	 */
	
	public boolean loadBPartner (int C_BPartner_ID)
	{
		log.config("C_BPartner_ID=" + C_BPartner_ID);
		
		//  New bpartner
		if (C_BPartner_ID == 0)
		{
			partner = null;
			m_pLocation = null;
			m_user = null;
			return true;
		}

		partner = new MBPartner (Env.getCtx(), C_BPartner_ID, null);
		
		if (partner.get_ID() == 0)
		{
			FDialog.error(m_WindowNo, this, "BPartnerNotFound");
			return false;
		}

		//	BPartner - Load values
		fValue.setText(partner.getValue());
		
		fName.setText(partner.getName());
		fName2.setText(partner.getName2());

		//	Contact - Load values
		m_pLocation = partner.getLocation(
			Env.getContextAsInt(Env.getCtx(), m_WindowNo, "C_BPartner_Location_ID"));
		
		if (m_pLocation != null)
		{
			int location = m_pLocation.getC_Location_ID();
			fAddress.setValue (location);
			
			fPhone.setText(m_pLocation.getPhone());
			fPhone2.setText(m_pLocation.getPhone2());
		}
		//	User - Load values
		m_user = partner.getContact(
			Env.getContextAsInt(Env.getCtx(), m_WindowNo, "AD_User_ID"));
		
		if (m_user != null)
		{
			
			fContact.setText(m_user.getName());
			fEMail.setText(m_user.getEMail());
			
			fPhone.setText(m_user.getPhone());
			fPhone2.setText(m_user.getPhone2());
		}
		return true;
	}	//	loadBPartner

	/**
	 *	Save.
	 *	Checks mandatory fields and saves Partner, Contact and Location
	 * 	@return true if saved
	 */
	
	private boolean actionSave()
	{
		log.config("");

		//	Check Mandatory fields
		if (fName.getText().equals(""))
		{
			throw new WrongValueException(fName, Msg.translate(Env.getCtx(), "FillMandatory"));
		}
			
		if (fAddress.getC_Location_ID() == 0)
		{
			throw new WrongValueException(fAddress.getComponent(), Msg.translate(Env.getCtx(), "FillMandatory"));
		}

		//	***** Business Partner *****
		
		if (partner == null)
		{
			int AD_Client_ID = Env.getAD_Client_ID(Env.getCtx());
			partner = MBPartnerPOS.getTemplate(Env.getCtx(), AD_Client_ID, pos.getC_POS_ID());
			partner.setAD_Org_ID(Env.getAD_Org_ID(Env.getCtx())); // Elaine 2009/07/03
			boolean isSOTrx = !"N".equals(Env.getContext(Env.getCtx(), m_WindowNo, "IsSOTrx"));
			partner.setIsCustomer (isSOTrx);
			partner.setIsVendor (!isSOTrx);
		}
		
		//	Check Value
		
		String value = fValue.getText();
		
		if (value == null || value.length() == 0)
		{
			//	get Table Document No
			value = DB.getDocumentNo (Env.getAD_Client_ID(Env.getCtx()), "C_BPartner", null);
			fValue.setText(value);
		}
		
		partner.setValue(fValue.getText());
		
		partner.setName(fName.getText());
		partner.setName2(fName2.getText());
		
		
		if (partner.save())
			log.fine("C_BPartner_ID=" + partner.getC_BPartner_ID());
		else
			FDialog.error(m_WindowNo, this, "BPartnerNotSaved");
		
		//	***** Business Partner - Location *****
		
		if (m_pLocation == null)
			m_pLocation = new MBPartnerLocation(partner);
		
		m_pLocation.setC_Location_ID(fAddress.getC_Location_ID());

		m_pLocation.setPhone(fPhone.getText());
		m_pLocation.setPhone2(fPhone2.getText());
		
		if (m_pLocation.save())
			log.fine("C_BPartner_Location_ID=" + m_pLocation.getC_BPartner_Location_ID());
		else
			FDialog.error(m_WindowNo, this, "BPartnerNotSaved", Msg.translate(Env.getCtx(), "C_BPartner_Location_ID"));
			
		//	***** Business Partner - User *****
		
		String contact = fContact.getText();
		String email = fEMail.getText();
		
		if (m_user == null && (contact.length() > 0 || email.length() > 0))
			m_user = new MUser (partner);
		
		if (m_user != null)
		{
			if (contact.length() == 0)
				contact = fName.getText();
		
			m_user.setName(contact);
			m_user.setEMail(email);
			
			m_user.setPhone(fPhone.getText());
			m_user.setPhone2(fPhone2.getText());
			
			if (m_user.save())
				log.fine("AD_User_ID=" + m_user.getAD_User_ID());
			else
				FDialog.error(m_WindowNo, this, "BPartnerNotSaved", Msg.translate(Env.getCtx(), "AD_User_ID"));
		}
		return true;
	}	//	actionSave

	/**
	 *	Returns BPartner ID
	 *	@return C_BPartner_ID (0 = not saved)
	 */
	
	public int getC_BPartner_ID()
	{
		if (partner == null)
			return 0;
		
		return partner.getC_BPartner_ID();
	}	//	getBPartner_ID

	public String showKeyboard(Event e){
		isKeyboard = true;
		Textbox field = (Textbox) e.getTarget();

//		WPOSKeyboard keyboard = null;//pos.getKeyboard();
//	  	if(keyboard != null){
//	  		if(e.getName().equals(Events.ON_FOCUS)){
//	  			keyboard.setPosTextField(field);	
//	  			AEnv.showWindow(keyboard);
//	  		}
//		}
		return field.getText();
	}
	
	public void onEvent(Event e) throws Exception 
	{
		if (m_readOnly)
			this.detach();
		//  Fixed error #538
		String m_Text;
		//	copy value
		if(e.getTarget().equals(fValue.getComponent(WPOSTextField.SECONDARY)) && !isKeyboard) {
			m_Text = showKeyboard(e);
			if(m_Text.length() > 0)
				fValue.setValue(m_Text);
			fValue.setFocus(true);
		} else if(e.getTarget().equals(fName.getComponent(WPOSTextField.SECONDARY)) && !isKeyboard) {
			m_Text = showKeyboard(e);
			if(m_Text.length() > 0)
				fName.setValue(m_Text);
			fName.setFocus(true);
		} else if(e.getTarget().equals(fName2.getComponent(WPOSTextField.SECONDARY)) && !isKeyboard) {
			m_Text = showKeyboard(e);
			if(m_Text.length() > 0)
				fName2.setValue(m_Text);
			fName2.setFocus(true);
		} else if(e.getTarget().equals(fContact.getComponent(WPOSTextField.SECONDARY)) && !isKeyboard) {
			m_Text = showKeyboard(e);
			if(m_Text.length() > 0)
				fContact.setValue(m_Text);
			fContact.setFocus(true);
		} else if(e.getTarget().equals(fEMail.getComponent(WPOSTextField.SECONDARY)) && !isKeyboard) {
			m_Text = showKeyboard(e);
			if(m_Text.length() > 0)
				fEMail.setValue(m_Text);
			fEMail.setFocus(true);
		} else if(e.getTarget().equals(fPhone.getComponent(WPOSTextField.SECONDARY)) && !isKeyboard) {
			m_Text = showKeyboard(e);
			if(m_Text.length() > 0)
				fPhone.setValue(m_Text);
			fPhone.setFocus(true);
		} else if(e.getTarget().equals(fPhone2.getComponent(WPOSTextField.SECONDARY)) && !isKeyboard){
			m_Text = showKeyboard(e);
			if(m_Text.length() > 0)
				fPhone2.setValue(m_Text);
			fPhone2.setFocus(true);
		}
		 // End
		else if(e.getTarget().equals(fValue.getComponent(WPOSTextField.PRIMARY)) 
				|| e.getTarget().equals(fName.getComponent(WPOSTextField.PRIMARY))
				|| e.getTarget().equals(fName2.getComponent(WPOSTextField.PRIMARY))
				|| e.getTarget().equals(fContact.getComponent(WPOSTextField.PRIMARY))
				|| e.getTarget().equals(fEMail.getComponent(WPOSTextField.PRIMARY))
				|| e.getTarget().equals(fPhone.getComponent(WPOSTextField.PRIMARY))
				|| e.getTarget().equals(fPhone2.getComponent(WPOSTextField.PRIMARY))) {

			 isKeyboard = false;
		}
		//	OK pressed
		else if ((e.getTarget() == confirmPanel.getButton("Ok")) && actionSave())
			this.detach();
		
		//	Cancel pressed
		else if (e.getTarget() == confirmPanel.getButton("Cancel"))
			this.detach();
	}
	
}