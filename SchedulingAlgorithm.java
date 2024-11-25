import java.util.*;
import java.io.*;

public class SchedulingAlgorithm {

    public static Results Run(int runtime, Vector<sProcess> processVector, Results result, int timeSlice) {
        String resultsFile = "Summary-Processes";

        try {
            PrintStream out = new PrintStream(new FileOutputStream(resultsFile));
            // Call round robin here
            roundRobin(runtime, timeSlice, processVector, result, out);
            // Uncomment the following line to use FIFO instead
            // FIFO(runtime, processVector, result, out);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    private static void roundRobin(int runtime, int timeSlice, Vector<sProcess> processVector, Results result, PrintStream out) {
        int comptime = 0; // Total elapsed time
        int size = processVector.size(); // Number of processes
        int completed = 0; // Number of completed processes
        int currentProcess = 0; // Index of the current process

        result.schedulingType = "Batch (Preemptive)";
        result.schedulingName = "Round Robin";

        while (comptime < runtime && completed < size) {
            sProcess process = (sProcess) processVector.elementAt(currentProcess);

            // If the process is already completed, skip to the next one
            if (process.cpudone >= process.cputime) {
                currentProcess = (currentProcess + 1) % size;
                continue;
            }

            // Print the registered process
            printRegistered(out, currentProcess, process, comptime);

            // Determine how much time the process can run
            int timeToRun = Math.min(timeSlice, process.cputime - process.cpudone);

            for (int time = 0; time < timeToRun; time++) {
                // Simulate the process running
                process.cpudone++;
                comptime++;

                // Check for I/O blocking
                if (process.ioblocking > 0 && process.ionext < process.ioblocking) {
                    process.ionext++;
                    if (process.ionext == process.ioblocking) {
                        printIOBlocked(out, currentProcess, process, comptime);
                        process.numblocked++;
                        process.ionext = 0; // Reset I/O next for the next time
                        break; // Exit the loop to switch processes
                    }
                }

                // If the process completes during its time slice
                if (process.cpudone >= process.cputime) {
                    completed++;
                    printCompleted(out, currentProcess, process, comptime);
                    break; // Exit the loop to switch processes
                }
            }

            // Move to the next process in a circular manner
            currentProcess = (currentProcess + 1) % size;
        }

        if (completed < size) {
            out.println("Not enough time to complete all processes!");
        }
    }

    private static void printRegistered(PrintStream out, int currentProcess, sProcess process, int comptime) {
        out.println("Process: " + currentProcess + " registered... (" + process.cputime + " " + process.ioblocking + " " + process.cpudone + " " + comptime + ")");
    }

    private static void printCompleted(PrintStream out, int currentProcess, sProcess process, int comptime) {
        out.println("Process: " + currentProcess + " completed... (" + process.cputime + " " + process.ioblocking + " " + process.cpudone + " " + comptime + ")");
    }

    private static void printIOBlocked(PrintStream out, int currentProcess, sProcess process, int comptime) {
        out.println("Process: " + currentProcess + " I/O blocked... (" + process.cputime + " " + process.ioblocking + " " + process.cpudone + " " + comptime + ")");
    }
}
