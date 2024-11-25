import java.util.*;
import java.io.*;

public class SchedulingAlgorithm {

  public static Results Run(int runtime, Vector<sProcess> processVector, Results result, int timeSlice) {

    String resultsFile = "Summary-Processes";

    try {
      PrintStream out = new PrintStream(new FileOutputStream(resultsFile));
      roundRobin(runtime, timeSlice, processVector, result, out);
      out.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return result;
  }

  private static void roundRobin(int runtime, int timeSlice, Vector<sProcess> processVector, Results result, PrintStream out){
    int comptime = 0;
    int size = processVector.size();
    Queue<sProcess> queue = new LinkedList<>(processVector);

    result.schedulingType = "Batch (Preemptive)";
    result.schedulingName = "Round-Robin";

    // Initial registration and I/O blocking of all processes
    for (sProcess process : processVector) {
      int currentProcessIndex = processVector.indexOf(process);
      printRegistered(out, currentProcessIndex, process, comptime);

      if (process.ioblocking > 0 && process.ionext == process.ioblocking) {
        printIOBlocked(out, currentProcessIndex, process, comptime);
        process.numblocked++;
        process.ionext = 0;
      }
    }

    while (comptime < runtime && !queue.isEmpty()) {
        sProcess process = queue.poll();
        if (process.cpudone < process.cputime) {
            int timeToRun = Math.min(timeSlice, process.cputime - process.cpudone);
            process.cpudone += timeToRun;
            comptime += timeToRun;

            int currentProcessIndex = processVector.indexOf(process);
            printRegistered(out, currentProcessIndex, process, comptime);

            if (process.ioblocking > 0 && process.ionext == process.ioblocking) {
                printIOBlocked(out, currentProcessIndex, process, comptime);
                process.numblocked++;
                process.ionext = 0;
            }

            if (process.cpudone == process.cputime) {
                printCompleted(out, currentProcessIndex, process, comptime);
            } else {
                queue.add(process);
            }

            if (process.ioblocking > 0 && process.ionext == process.ioblocking) {
                printIOBlocked(out, currentProcessIndex, process, comptime);
                process.numblocked++;
                process.ionext = 0;
            } else {
                process.ionext += timeToRun;
            }
        }
    }

    result.compuTime = comptime;
  }

  private static void FIFO(int runtime, Vector<sProcess> processVector, Results result, PrintStream out){
    int i = 0;
    int comptime = 0;
    int currentProcess = 0;
    int previousProcess = 0;
    int size = processVector.size();
    int completed = 0;

    result.schedulingType = "Batch (Nonpreemptive)";
    result.schedulingName = "First-Come First-Served";

    try {
      sProcess process = (sProcess) processVector.elementAt(currentProcess);
      printRegistered(out, currentProcess, process, comptime);
      while (comptime < runtime) {

        // Check completion of the process
        if (process.cpudone == process.cputime) {
          completed++;
          printCompleted(out, currentProcess, process, comptime);
          if (completed == size) {
            result.compuTime = comptime;
            return;
          }
          // scheduling the next process
          for (i = size - 1; i >= 0; i--) {
            process = (sProcess) processVector.elementAt(i);
            if (process.cpudone < process.cputime) {
              currentProcess = i;
            }
          }
          process = (sProcess) processVector.elementAt(currentProcess);
          printRegistered(out, currentProcess, process, comptime);

        }
        // Checking for blocking time
        if (process.ioblocking == process.ionext) {
          printIOBlocked(out, currentProcess, process, comptime);
          process.numblocked++;
          process.ionext = 0;

          // scheduling the next process
          previousProcess = currentProcess;
          for (i = size - 1; i >= 0; i--) {
            process = (sProcess) processVector.elementAt(i);
            if (process.cpudone < process.cputime && previousProcess != i) {
              currentProcess = i;
            }
          }
          process = (sProcess) processVector.elementAt(currentProcess);
          printRegistered(out, currentProcess, process, comptime);
        }
        // increment timer counters
        process.cpudone++;
        if (process.ioblocking > 0) {
          process.ionext++;
        }
        comptime++;
      }
      out.println("Not enough time to complete all processes!");
    } catch (Exception e) {
      result.compuTime = comptime;
      throw(e);
    }
  }

  private static void printRegistered(PrintStream out, int currentProcess, sProcess process, int comptime){
    out.println("Process: " + currentProcess + " registered... (" + process.cputime + " " + process.ioblocking + " " + process.cpudone + " " + comptime + ")");
  }

  private static void printCompleted(PrintStream out, int currentProcess, sProcess process, int comptime){
    out.println("Process: " + currentProcess + " completed... (" + process.cputime + " " + process.ioblocking + " " + process.cpudone + " " + comptime + ")");
  }

  private static void printIOBlocked(PrintStream out, int currentProcess, sProcess process, int comptime){
    out.println("Process: " + currentProcess + " I/O blocked... (" + process.cputime + " " + process.ioblocking + " " + process.cpudone + " " + comptime + ")");
  }
}
