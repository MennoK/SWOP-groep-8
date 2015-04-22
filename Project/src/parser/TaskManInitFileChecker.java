package parser;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;


enum TaskStatus { UNSPECIFIED, EXECUTING, FINISHED, FAILED }

public class TaskManInitFileChecker extends StreamTokenizer {

  public static void main(String[] args) throws FileNotFoundException {
    if (args.length < 1){
      System.err.println("Error: First command line argument must be filename.");
    }else{
      new TaskManInitFileChecker(new FileReader(args[0])).checkFile();
    }
  }

  DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
  DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

  TaskManInitFileChecker(Reader r) {
    super(r);
  }

  public int nextToken() {
    try {
      return super.nextToken();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  void error(String msg) {
    throw new RuntimeException("Line " + lineno() + ": " + msg);
  }

  boolean isWord(String word) {
    return ttype == TT_WORD && sval.equals(word);
  }

  void expectChar(char c) {
    if (ttype != c)
      error ("'" + c + "' expected");
    nextToken();
  }

  void expectLabel(String name) {
    if (!isWord(name))
      error("Keyword '" + name + "' expected");
    nextToken();
    expectChar(':');
  }

  String expectStringField(String label) {
    expectLabel(label);
    if (ttype != '"')
      error("String expected");
    String value = sval;
    nextToken();
    return value;
  }

  LocalDateTime expectDateField(String label) {
    String date = expectStringField(label);
    return LocalDateTime.parse(date, dateTimeFormatter);
  }

  LocalTime expectTimeField(String label) {
    String date = expectStringField(label);
    return LocalTime.parse(date, timeFormatter);
  }

  int expectInt() {
    if (ttype != TT_NUMBER || nval != (double)(int)nval)
      error("Integer expected");
    int value = (int)nval;
    nextToken();
    return value;
  }

  int expectIntField(String label) {
    expectLabel(label);
    return expectInt();
  }

  List<Integer> expectIntList() {
    ArrayList<Integer> list = new ArrayList<>();
    expectChar('[');
    while (ttype == TT_NUMBER){
      list.add(expectInt());
      if (ttype == ',')
        expectChar(',');
      else if (ttype != ']')
        error("']' (end of list) or ',' (new list item) expected");
    }
    expectChar(']');
    return list;
  }

  public class IntPair
  {
    public int first;
    public int second;
  }
  
  public class IntListPair
  {
	  public int first;
	  public List<Integer> second;
  }

  List<IntPair> expectLabeledPairList(String first, String second) {
    ArrayList<IntPair> list = new ArrayList<>();
    expectChar('[');
    while (ttype == '{'){
      if (ttype == '{')
      {
        expectChar('{');
        int f = expectIntField(first);
        expectChar(',');
        int s = expectIntField(second);
        expectChar('}');
        IntPair p = new IntPair();
        p.first = f;
        p.second = s;
        list.add(p);
      }
      if (ttype == ',')
        expectChar(',');
      else if (ttype != ']')
        error("']' (end of list) or ',' (new list item) expected");
    }
    expectChar(']');
    return list;
  }
  
   List<IntListPair> expectLabeledIntListPairList(String first,
			String second) {
	  List<IntListPair> list = new ArrayList<>();
	    expectChar('[');
	    while (ttype == '{'){
	      if (ttype == '{')
	      {
	        expectChar('{');
	        int f = expectIntField(first);
	        expectChar(',');
	        expectLabel(second);
	        List<Integer> s = expectIntList();
	        expectChar('}');
	        IntListPair p = new IntListPair();
	        p.first = f;
	        p.second = s;
	        list.add(p);
	      }
	      if (ttype == ',')
	        expectChar(',');
	      else if (ttype != ']')
	        error("']' (end of list) or ',' (new list item) expected");
	    }
	    expectChar(']');
	    return list;
	}

  @SuppressWarnings("unused")
void checkFile() {
    slashSlashComments(false);
    slashStarComments(false);
    ordinaryChar('/'); // otherwise "//" keeps treated as comments.
    commentChar('#');

    nextToken();

    LocalDateTime systemTime = expectDateField("systemTime");

    expectLabel("dailyAvailability");
    while (ttype == '-') {
      expectChar('-');
      LocalTime creationTime = expectTimeField("startTime");
      LocalTime dueTime = expectTimeField("endTime");
    }

    expectLabel("resourceTypes");
    while (ttype == '-') {
      expectChar('-');
      String name = expectStringField("name");
      expectLabel("requires");
      List<Integer> requirements = expectIntList();
      expectLabel("conflictsWith");
      List<Integer> conflicts = expectIntList();
      expectLabel("dailyAvailability");
      if (ttype == TT_NUMBER)
      {
        int availabilityIndex = expectInt();
      }
    }

    expectLabel("resources");
    while (ttype == '-') {
      expectChar('-');
      String name = expectStringField("name");
      expectLabel("type");
      int typeIndex = expectInt();
    }

    expectLabel("developers");
    while (ttype == '-') {
      expectChar('-');
      String name = expectStringField("name");
    }

    expectLabel("projects");
    while (ttype == '-') {
      expectChar('-');
      String name = expectStringField("name");
      String description = expectStringField("description");
      LocalDateTime creationTime = expectDateField("creationTime");
      LocalDateTime dueTime = expectDateField("dueTime");
    }

    expectLabel("tasks");
    while (ttype == '-') {
      expectChar('-');
      int project = expectIntField("project");
      String description = expectStringField("description");
      int estimatedDuration = expectIntField("estimatedDuration");
      int acceptableDeviation = expectIntField("acceptableDeviation");
      Integer alternativeFor = null;
      expectLabel("alternativeFor");
      if (ttype == TT_NUMBER)
        alternativeFor = expectInt();
      List<Integer> prerequisiteTasks = new ArrayList<>();
      expectLabel("requiredTypes");
      List<IntPair> resources = expectLabeledPairList("type", "quantity");
      expectLabel("prerequisiteTasks");
      if (ttype == '[')
        prerequisiteTasks = expectIntList();
      expectLabel("status");
      TaskStatus status = TaskStatus.UNSPECIFIED;
      if (isWord("executing")) {
        nextToken();
        status = TaskStatus.EXECUTING;
      } else if (isWord("finished")) {
        nextToken();
        status = TaskStatus.FINISHED;
      } else if (isWord("failed")) {
        nextToken();
        status = TaskStatus.FAILED;
      }
      if (status != TaskStatus.UNSPECIFIED && status != TaskStatus.EXECUTING) {
        LocalDateTime startTime = expectDateField("startTime");
        LocalDateTime endTime = expectDateField("endTime");
      }
    }
    
    expectLabel("plannings");
    while (ttype == '-') {
      expectChar('-');
      LocalDateTime dueTime = expectDateField("plannedStartTime");
      expectLabel("developers");
      List<Integer> developers = expectIntList();
      int task = expectIntField("task");
      expectLabel("resources");
      List<IntListPair> resources = expectLabeledIntListPairList("type", "resource");
    }
    
    if (ttype != TT_EOF)
      error("End of file or '-' expected");
  }
}
