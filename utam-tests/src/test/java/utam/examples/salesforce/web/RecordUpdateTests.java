/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: MIT
 * For full license text, see the LICENSE file in the repo root
 * or https://opensource.org/licenses/MIT
 */
package utam.examples.salesforce.web;

import static org.testng.Assert.assertEquals;

import java.text.DateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import utam.aura.pageobjects.InputCheckbox;
import utam.aura.pageobjects.impl.VirtualDataTableImpl;
import utam.core.element.BasicElement;
import utam.core.framework.context.StringValueProfile;
import utam.flexipage.pageobjects.Tab2;
import utam.force.pageobjects.ListViewManagerHeader;
import utam.force.pageobjects.ObjectHome;
import utam.force.pageobjects.VirtualCheckbox;
import utam.force.pageobjects.impl.VirtualCheckboxImpl;
import utam.global.pageobjects.AppNavBar;
import utam.global.pageobjects.ConsoleObjectHome;
import utam.global.pageobjects.RecordActionWrapper;
import utam.global.pageobjects.RecordHomeFlexipage2;
import utam.lightning.pageobjects.*;
import utam.navex.pageobjects.DesktopLayoutContainer;
import utam.records.pageobjects.BaseRecordForm;
import utam.records.pageobjects.LwcDetailPanel;
import utam.records.pageobjects.LwcHighlightsPanel;
import utam.records.pageobjects.LwcRecordLayout;
import utam.records.pageobjects.RecordLayoutItem;
import utam.runtime_sales.pageobjects.PathAssistantTab;
import utam.runtime_sales.pageobjects.PathAssistantTabSet;
import utam.runtime_sales.pageobjects.PathassistantCollapsibleDrawer;
import utam.utils.salesforce.RecordType;
import utam.utils.salesforce.TestEnvironment;

/**
 * IMPORTANT: Page objects and tests for Salesforce UI are compatible with application version
 * mentioned in published page objects. Test environment is private SF sandbox, not available for
 * external users and has DEFAULT org setup
 *
 * @author Salesforce
 * @since Dec 2021
 */
public class RecordUpdateTests extends SalesforceWebTestBase {

  private final TestEnvironment testEnvironment = getTestEnvironment("sandbox");

  @BeforeTest
  public void setup() {
    setupChrome();
    login(testEnvironment, "home");
  }

  private void gotoRecordHomeByUrl(RecordType recordType, String recordId) {
    String recordHomeUrl = recordType.getRecordHomeUrl(testEnvironment.getRedirectUrl(), recordId);
    log("Navigate to the Record Home by URL: " + recordHomeUrl);
    getDriver().get(recordHomeUrl);
  }

  @Test
  public void testEditAccountRecord() {

    // todo - replace with existing Account Id for the environment
    final String accountRecordId = testEnvironment.getAccountId();
    gotoRecordHomeByUrl(RecordType.Account, accountRecordId);

    log("Load Accounts Record Home page");
    RecordHomeFlexipage2 recordHome = from(RecordHomeFlexipage2.class);

    log("Access Record Highlights panel");
    LwcHighlightsPanel highlightsPanel = recordHome.getHighlights();

    log("Wait for button 'Edit' and click on it");
    highlightsPanel.getActions().getActionRendererWithTitle("Edit").clickButton();

    log("Load Record Form Modal");
    RecordActionWrapper recordFormModal = from(RecordActionWrapper.class);
    BaseRecordForm recordForm = recordFormModal.getRecordForm();
    LwcRecordLayout recordLayout = recordForm.getRecordLayout();

    log("Access record form item by index");
    RecordLayoutItem item = recordLayout.getItem(1, 2, 1);

    log("Enter updated account name");
    final String accountName = "Utam";
    item.getTextInput().setText(accountName);

    log("Save updated record");
    recordForm.clickFooterButton("Save");
    recordFormModal.waitForAbsence();
  }

  @Test
  public void testInlineEditContactRecord() {

    // todo - replace with existing Contact Id for the environment
    final String recordId = testEnvironment.getContactId();
    gotoRecordHomeByUrl(RecordType.Contact, recordId);

    RecordHomeFlexipage2 recordHome = from(RecordHomeFlexipage2.class);
    Tabset tabset = recordHome.getTabset();

    log("Select 'Details' tab");
    TabBar tabBar = tabset.getTabBar();
    String activeTabName = tabBar.getActiveTabText();
    if (!"Details".equalsIgnoreCase(activeTabName)) {
      tabBar.clickTab("Details");
    }
    log("Access Name field on Details panel");
    LwcDetailPanel detailPanel = tabset.getActiveTabContent(Tab2.class).getDetailPanel();
    LwcRecordLayout recordLayout = detailPanel.getBaseRecordForm().getRecordLayout();
    RecordLayoutItem nameItem = recordLayout.getItem(1, 2, 1);

    log("Remember value of the name field");
    String nameString = nameItem.getFormattedName().getInnerText();

    log("Click inline edit (pencil) next to the Name field");
    nameItem.getInlineEditButton().click();

    log("Click Save at the bottom of Details panel");
    Button saveButton = detailPanel
        .getBaseRecordForm()
        .getFooter()
        .getActionsRibbon()
        .getActionRendererWithTitle("Save")
        .getHeadlessAction()
        .getLightningButton();
    saveButton.click();
    saveButton.waitForAbsence();

    log("Wait for field to be updated");
    nameItem.waitForOutputField();
    log("Check that field value has not changed");
    assertEquals(nameItem.getFormattedName().getInnerText(), nameString);
  }

  @Test
  public void testEditLeadRecord() {
    // set profile to lead entity type
    // loader.getConfig().setProfile(new StringValueProfile("entity", "lead"));
    // loader.resetContext();
    setProfile(RecordType.Lead);

    // todo - replace with existing Lead Id for the environment
    final String leadId = testEnvironment.getLeadId();
    gotoRecordHomeByUrl(RecordType.Lead, leadId);

    log("Load Lead Record Home page");
    RecordHomeFlexipage2 recordHome = from(RecordHomeFlexipage2.class);

    log("Access Lead Highlights panel");
    LwcHighlightsPanel highlightsPanel = recordHome.getHighlights();

    log("Wait for button 'Edit' and click on it");
    highlightsPanel.getActions().getActionRendererWithTitle("Edit").clickButton();

    log("Load Record Form Modal");
    RecordActionWrapper recordFormModal = from(RecordActionWrapper.class);
    BaseRecordForm recordForm = recordFormModal.getRecordForm();
    LwcRecordLayout recordLayout = recordForm.getRecordLayout();

    log("Access record form item by index");
    RecordLayoutItem item = recordLayout.getItem(1, 3, 1);

    log("Enter updated lead company name");
    final String formattedDate =
        DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
            .format(Calendar.getInstance().getTime());
    final String updatedLeadCompanyName = "Utam and Co. updated on " + formattedDate;
    item.getTextInput().setText(updatedLeadCompanyName);

    log("Save updated record");
    recordForm.clickFooterButton("Save");
    recordFormModal.waitForAbsence();
  }

  @Test
  public void contactsPageTitle() {
    final String recordId = "0035i00003KW7VpAAL";
    gotoRecordHomeByUrl(RecordType.Contact, recordId);

    RecordHomeFlexipage2 recordHome = from(RecordHomeFlexipage2.class);
    Tabset tabset = recordHome.getTabset();

    TabBar tabBar = tabset.getTabBar();
    String activeTabName = tabBar.getActiveTabText();
    if (!"Details".equalsIgnoreCase(activeTabName)) {
      tabBar.clickTab("Details");
    }
    LwcDetailPanel detailPanel = tabset.getActiveTabContent(Tab2.class).getDetailPanel();
    LwcRecordLayout recordLayout = detailPanel.getBaseRecordForm().getRecordLayout();
    RecordLayoutItem nameItem = recordLayout.getItem(1, 2, 1);

    String nameString = nameItem.getFormattedName().getInnerText();
    System.out.println("nameString is " + nameString);
    Assert.assertEquals(nameString, "Mr. Girsih Kumar");

    tabBar.getTabByLabel("News").click();


    LwcHighlightsPanel highlightsPanel = recordHome.getHighlights();
    highlightsPanel.getActions().getActionRendererWithTitle("Edit").clickButton();


  }

  @Test
  public void leadStatusChange() throws InterruptedException {
    final String recordId = "00Q5i00000JeaiwEAB";
    gotoRecordHomeByUrl(RecordType.Lead, recordId);

    PathassistantCollapsibleDrawer pathassistantCollapsibleDrawer = from(PathassistantCollapsibleDrawer.class);

    PathAssistantTabSet tabSet = pathassistantCollapsibleDrawer.getPathAssistantTabSet();

    String activeStage = tabSet.getActiveTab().getDataName();
    System.out.println("activeStage is " + activeStage);

    Thread.sleep(20000);

    PathAssistantTab tabToBeClicked = tabSet.getTabByName("Open - Not Contacted");
    tabToBeClicked.getClickHeader().clickAndHold(1);

    pathassistantCollapsibleDrawer.getCompleteStepActionButton().clickAndHold(1);

  }

  @Test
  public void leadListPage() throws InterruptedException {
    getDriver().get(testEnvironment.getRedirectUrl());
    log("Load Desktop layout container");
    DesktopLayoutContainer layoutContainer = from(DesktopLayoutContainer.class);

    log("Navigate to nav bar item contacts");
    AppNavBar navBar = layoutContainer.getAppNav().getAppNavBar();
    navBar.getNavItem("Lead").clickAndWaitForUrl("Lead");


    VirtualDataTableImpl table = loader.load(ObjectHome.class)
            .getListView()
            .getListViewContainer(VirtualDataTableImpl.class);

    int rowsCount = table.getRowsCount();
    int columnsCount = table.getColumnsCount();
    List<String> headerTitles = table.getHeaderTitles();

    System.out.println("rowsCount: " + rowsCount);
    System.out.println("columnsCount: " + columnsCount);
    System.out.println("Number of headerTitles: " + headerTitles.size());
    System.out.println("headerTitles: " + Arrays.toString(headerTitles.toArray()));

    List<BasicElement> headerCells = table.getHeaderCells();
    System.out.println("headerCells: " + Arrays.toString(headerCells.toArray()));


    //Get text of every column in first row
    for (int column = 3; column < headerTitles.size(); ) {
      System.out.println("Text: " + table.getCellText(1, column));
      column ++;
    }


    // Clicks on name header
    table.clickHeaderSpecialLink(3);
    table.getCellSpecialText(1);


    //clickCellSpecialLink works for Lead Name; Click on 2nd lead name
    System.out.println(table.getCellSpecialText(2));
//        table.clickCellSpecialLink(2);

    //select Checkbox
//        table.getCellComponent(1, 2, VirtualCheckboxImpl.class).getRootElement().click();

    WebElement element = getDriver().findElement(By.cssSelector("tr:nth-of-type(1) td div.forceVirtualCheckbox"));
    element.click();

    table.getCellComponent(2, 2, VirtualCheckbox.class).toggleCheckbox();

    table.getCellComponent(2, 2, VirtualCheckboxImpl.class).toggleCheckbox();

    table.getCellComponent(2, 2, InputCheckbox.class).toggleCheckbox();
    table.getCellOverlayComponent(2,3, VirtualCheckbox.class).toggleCheckbox();
  }

  @Test
  public void filterLeads() throws InterruptedException {
    getDriver().get(testEnvironment.getRedirectUrl());
    log("Load Desktop layout container");
    DesktopLayoutContainer layoutContainer = from(DesktopLayoutContainer.class);

    log("Navigate to nav bar item contacts");
    AppNavBar navBar = layoutContainer.getAppNav().getAppNavBar();
    navBar.getNavItem("Lead").clickAndWaitForUrl("Lead");


    VirtualDataTableImpl table = loader.load(ObjectHome.class)
            .getListView()
            .getListViewContainer(VirtualDataTableImpl.class);

    int rowsCount = table.getRowsCount();
    int columnsCount = table.getColumnsCount();
    List<String> headerTitles = table.getHeaderTitles();

    System.out.println("rowsCount: " + rowsCount);
    System.out.println("columnsCount: " + columnsCount);
    System.out.println("Number of headerTitles: " + headerTitles.size());
    System.out.println("headerTitles: " + Arrays.toString(headerTitles.toArray()));


    ListViewManagerHeader listViewManagerHeader = loader.load(ConsoleObjectHome.class)
            .getListView()
            .getHeader();

    String viewName = listViewManagerHeader.getSelectedListViewName();
    System.out.println("viewName is " + viewName);

    listViewManagerHeader.getListViewManagerButtonBar().getIcon().clickButtonWithTitle("Show filters");

  }
  @Test
  public void dateTimeComponent() throws InterruptedException {
    final String recordId = "00Q5i00000KahB5EAJ";
    gotoRecordHomeByUrl(RecordType.Lead, recordId);

    log("Load Accounts Record Home page");
    RecordHomeFlexipage2 recordHome = from(RecordHomeFlexipage2.class);

    log("Access Record Highlights panel");
    LwcHighlightsPanel highlightsPanel = recordHome.getHighlights();

    log("Wait for button 'Edit' and click on it");
    highlightsPanel.getActions().getActionRendererWithTitle("Edit").clickButton();

    log("Access record form item by index");
    RecordActionWrapper recordFormModal = from(RecordActionWrapper.class);
    BaseRecordForm recordForm = recordFormModal.getDetailPanel().getBaseRecordForm();

    RecordLayoutItem item =  recordForm.getRecordLayout().getItem(1, 9, 1);
    Datetimepicker datetimepicker = item.getTextInput().getDatetimepicker();
    Datepicker date = datetimepicker.getDatepicker();
    BaseCombobox time = datetimepicker.getTimepicker().getTimeCombobox();

    date.setDateText("12/11/2023");
    datetimepicker.getTimepicker().getTimeCombobox().getTriggerInput().clearAndType("1:00pm");

    log("Save updated record");
    recordForm.clickFooterButton("Save");
    recordFormModal.waitForAbsence();
  }

  @AfterTest
  public final void tearDown() {
    quitDriver();
  }
}
