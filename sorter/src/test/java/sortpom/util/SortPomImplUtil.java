package sortpom.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.util.List;
import sortpom.parameter.PluginParameters;

/** Test utility to enter sort parameters */
public class SortPomImplUtil {

  private TestHandler testHandler;

  private String customSortOrderFile;
  private String sortDependencies = "";
  private String sortDependencyExclusions = "";
  private String sortPlugins = "";
  private boolean sortProperties = false;
  private boolean sortModules = false;
  private boolean sortExecutions = false;
  private String predefinedSortOrder = "recommended_2008_06";
  private String lineSeparator = "\r\n";
  private String testPomFileName = "src/test/resources/testpom.xml";
  private String testPomBackupExtension = ".testExtension";

  private int nrOfIndentSpace = 2;
  private boolean keepBlankLines = true;
  private boolean ignoreLineSeparators = true;
  private boolean indentBLankLines = false;
  private boolean indentSchemaLocation = false;
  private boolean keepTimestamp = false;
  private String verifyFail = "SORT";
  private String verifyFailOn = "xmlElements";
  private String encoding = "UTF-8";
  private File testpom;
  private String violationFile;
  private boolean createBackupFile = true;

  private SortPomImplUtil() {}

  public static SortPomImplUtil create() {
    return new SortPomImplUtil();
  }

  public void testFiles(final String inputResourceFileName, final String expectedResourceFileName)
      throws Exception {
    setup();
    testHandler =
        new TestHandler(inputResourceFileName, expectedResourceFileName, getPluginParameters());
    testHandler.performSortThatSorted();
    List<String> infoLogger = testHandler.getInfoLogger();
    assertThat(infoLogger.get(0), startsWith("[INFO] Sorting file "));
    assertThat(infoLogger.get(1), startsWith("[INFO] Saved backup of "));
    assertThat(
        infoLogger.get(infoLogger.size() - 1), startsWith("[INFO] Saved sorted pom file to "));
  }

  public void testFilesWithTimestamp(
      final String inputResourceFileName, final String expectedResourceFileName) throws Exception {
    setup();
    testHandler =
        new TestHandler(inputResourceFileName, expectedResourceFileName, getPluginParameters());
    testHandler.performSortThatTestsTimestamps();
  }

  public void testNoSorting(final String inputResourceFileName) throws Exception {
    setup();
    testHandler =
        new TestHandler(inputResourceFileName, inputResourceFileName, getPluginParameters());
    testHandler.performSortThatDidNotSort();
    assertEquals("[INFO] Pom file is already sorted, exiting", testHandler.getInfoLogger().get(1));
  }

  public void testVerifyXmlIsOrdered(final String inputResourceFileName) throws Exception {
    setup();
    testHandler = new TestHandler(inputResourceFileName, getPluginParameters());
    XmlOrderedResult xmlOrderedResult = testHandler.performVerify();
    assertTrue(xmlOrderedResult.isOrdered(), "Expected that xml is ordered, ");
  }

  public void testVerifyXmlIsNotOrdered(
      final String inputResourceFileName, CharSequence warningMessage) throws Exception {
    setup();
    testHandler = new TestHandler(inputResourceFileName, getPluginParameters());
    XmlOrderedResult xmlOrderedResult = testHandler.performVerify();
    assertFalse(xmlOrderedResult.isOrdered(), "Expected that xml is not ordered, ");
    assertEquals(warningMessage, xmlOrderedResult.getErrorMessage());
  }

  public void testVerifySort(
      final String inputResourceFileName,
      final String expectedResourceFileName,
      String warningMessage,
      boolean outputToViolationFile)
      throws Exception {
    setup();
    testHandler =
        new TestHandler(inputResourceFileName, expectedResourceFileName, getPluginParameters());
    testHandler.performVerifyThatSorted();
    int index = assertStartOfMessages(warningMessage, outputToViolationFile);
    List<String> infoLogger = testHandler.getInfoLogger();
    assertThat(infoLogger.get(index), startsWith("[INFO] The file "));
    assertThat(infoLogger.get(index++), endsWith(" is not sorted"));
    assertThat(infoLogger.get(index), startsWith("[INFO] Sorting file "));
    assertThat(
        infoLogger.get(infoLogger.size() - 1), startsWith("[INFO] Saved sorted pom file to "));
  }

  public void testVerifyFail(
      String inputResourceFileName,
      Class<?> expectedExceptionClass,
      String warningMessage,
      boolean outputToViolationFile) {
    setup();
    testHandler = new TestHandler(inputResourceFileName, getPluginParameters());
    try {
      testHandler.performVerifyThatDidNotSort();
      fail();
    } catch (Exception e) {
      assertEquals(expectedExceptionClass, e.getClass());
      int index = assertStartOfMessages(warningMessage, outputToViolationFile);
      assertThat(testHandler.getInfoLogger().get(index), startsWith("[ERROR] The file "));
      assertThat(testHandler.getInfoLogger().get(index), endsWith(" is not sorted"));
    }
  }

  public void testVerifyWarn(
      String inputResourceFileName, String warningMessage, boolean outputToViolationFile)
      throws Exception {
    setup();
    testHandler =
        new TestHandler(inputResourceFileName, inputResourceFileName, getPluginParameters());
    testHandler.performVerifyThatDidNotSort();

    int index = assertStartOfMessages(warningMessage, outputToViolationFile);
    assertThat(testHandler.getInfoLogger().get(index), startsWith("[WARNING] The file "));
    assertThat(testHandler.getInfoLogger().get(index), endsWith(" is not sorted"));
  }

  private int assertStartOfMessages(String warningMessage, boolean outputToViolationFile) {
    int index = 0;
    assertThat(testHandler.getInfoLogger().get(index++), startsWith("[INFO] Verifying file "));
    assertEquals(warningMessage, testHandler.getInfoLogger().get(index++));

    if (outputToViolationFile) {
      assertThat(
          testHandler.getInfoLogger().get(index++),
          startsWith("[INFO] Saving violation report to "));
    }
    return index;
  }

  public SortPomImplUtil nrOfIndentSpace(int indent) {
    nrOfIndentSpace = indent;
    return this;
  }

  public SortPomImplUtil noKeepBlankLines() {
    keepBlankLines = false;
    return this;
  }

  public SortPomImplUtil indentBLankLines() {
    indentBLankLines = true;
    return this;
  }

  public SortPomImplUtil indentSchemaLocation() {
    indentSchemaLocation = true;
    return this;
  }

  public SortPomImplUtil sortDependencies(String sortOrder) {
    sortDependencies = sortOrder;
    return this;
  }

  public SortPomImplUtil sortDependencyExclusions(String sortOrder) {
    sortDependencyExclusions = sortOrder;
    return this;
  }

  public SortPomImplUtil sortPlugins(String sortOrder) {
    sortPlugins = sortOrder;
    return this;
  }

  public SortPomImplUtil sortProperties() {
    sortProperties = true;
    return this;
  }

  public SortPomImplUtil sortModules() {
    sortModules = true;
    return this;
  }

  public SortPomImplUtil sortExecutions() {
    sortExecutions = true;
    return this;
  }

  public SortPomImplUtil customSortOrderFile(String customSortOrderFile) {
    this.customSortOrderFile = customSortOrderFile;
    return this;
  }

  public SortPomImplUtil predefinedSortOrder(String predefinedSortOrder) {
    this.predefinedSortOrder = predefinedSortOrder;
    this.customSortOrderFile = null;
    return this;
  }

  public SortPomImplUtil lineSeparator(String lineSeparator) {
    this.lineSeparator = lineSeparator;
    return this;
  }

  public SortPomImplUtil ignoreLineSeparators(boolean ignoreLineSeparators) {
    this.ignoreLineSeparators = ignoreLineSeparators;
    return this;
  }

  public SortPomImplUtil keepTimestamp(boolean keepTimestamp) {
    this.keepTimestamp = keepTimestamp;
    return this;
  }

  public SortPomImplUtil verifyFail(String verifyFail) {
    this.verifyFail = verifyFail;
    return this;
  }

  public SortPomImplUtil verifyFailOn(String verifyFailOn) {
    this.verifyFailOn = verifyFailOn;
    return this;
  }

  public SortPomImplUtil backupFileExtension(String backupFileExtension) {
    this.testPomBackupExtension = backupFileExtension;
    return this;
  }

  public SortPomImplUtil encoding(String encoding) {
    this.encoding = encoding;
    return this;
  }

  public SortPomImplUtil testPomFileNameUniqueNumber(int uniqueNumber) {
    this.testPomFileName = "target/testpom" + uniqueNumber + ".xml";
    return this;
  }

  public SortPomImplUtil violationFile(String violationFile) {
    this.violationFile = violationFile;
    return this;
  }

  public SortPomImplUtil createBackupFile(boolean createBackupFile) {
    this.createBackupFile = createBackupFile;
    return this;
  }

  private void setup() {
    testpom = new File(testPomFileName);
  }

  private PluginParameters getPluginParameters() {
    return PluginParameters.builder()
        .setPomFile(testpom)
        .setFileOutput(createBackupFile, testPomBackupExtension, violationFile, keepTimestamp)
        .setEncoding(encoding)
        .setFormatting(lineSeparator, true, false, keepBlankLines)
        .setIndent(nrOfIndentSpace, indentBLankLines, indentSchemaLocation)
        .setSortEntities(
            sortDependencies,
            sortDependencyExclusions,
            sortPlugins,
            sortProperties,
            sortModules,
            sortExecutions)
        .setSortOrder(customSortOrderFile, predefinedSortOrder)
        .setVerifyFail(verifyFail, verifyFailOn)
        .setIgnoreLineSeparators(ignoreLineSeparators)
        .build();
  }
}
