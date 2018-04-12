package ca.queensu.cs.rover.configuration.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class AttributeNameDialog extends Dialog {
	
	/* This class is invoked in order 
	to create a dialogue box when changing 
	attribute properties within the configuration page */
	
	private Text attributeInput;
	private String name = "";
	  
	public AttributeNameDialog(Shell parentShell) {
	    super(parentShell);
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
	    Composite container = (Composite) super.createDialogArea(parent);
	    GridLayout layout = new GridLayout(2, false);
	    layout.marginRight = 5;
	    layout.marginLeft = 10;
	    container.setLayout(layout);

	    Label lblUser = new Label(container, SWT.NONE);
	    lblUser.setText("User:");

	    attributeInput = new Text(container, SWT.BORDER);
	    attributeInput.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
	    attributeInput.setText(name);
	    attributeInput.selectAll();
	    attributeInput.addModifyListener(new ModifyListener() {

	      @Override
	      public void modifyText(ModifyEvent e) {
	        Text textWidget = (Text) e.getSource();
	        String nameText = textWidget.getText();
	        name = nameText;
	      }
	    });

	    return container;
	  }

	  // override method to use "Login" as label for the OK button
	  @Override
	  protected void createButtonsForButtonBar(Composite parent) {
	    createButton(parent, IDialogConstants.OK_ID, "Login", true);
	    createButton(parent, IDialogConstants.CANCEL_ID,
	        IDialogConstants.CANCEL_LABEL, false);
	  }

	  @Override
	  protected Point getInitialSize() {
	    return new Point(450, 300);
	  }

	  @Override
	  protected void okPressed() {
	    name = attributeInput.getText();
	    super.okPressed();
	  }

	  public String getName() {
	    return name;
	  }

	  public void setName(String name) {
	    this.name = name;
	  }

}
