import java.util.*;

class Bank {
    String name;
    int netAmount;
    Set<String> types;

    Bank(String name, Set<String> types) {
        this.name = name;
        this.types = types;
        this.netAmount = 0;
    }
}

public class CashFlowMinimizer {
    private static int getMinIndex(Bank[] listOfNetAmounts, int numBanks) {
        int min = Integer.MAX_VALUE, minIndex = -1;
        for (int i = 0; i < numBanks; i++) {
            if (listOfNetAmounts[i].netAmount == 0) continue;

            if (listOfNetAmounts[i].netAmount < min) {
                minIndex = i;
                min = listOfNetAmounts[i].netAmount;
            }
        }
        return minIndex;
    }

    private static int getSimpleMaxIndex(Bank[] listOfNetAmounts, int numBanks) {
        int max = Integer.MIN_VALUE, maxIndex = -1;
        for (int i = 0; i < numBanks; i++) {
            if (listOfNetAmounts[i].netAmount == 0) continue;

            if (listOfNetAmounts[i].netAmount > max) {
                maxIndex = i;
                max = listOfNetAmounts[i].netAmount;
            }
        }
        return maxIndex;
    }

    private static Pair<Integer, String> getMaxIndex(Bank[] listOfNetAmounts, int numBanks, int minIndex, Bank[] input, int maxNumTypes) {
        int max = Integer.MIN_VALUE;
        int maxIndex = -1;
        String matchingType = "";

        for (int i = 0; i < numBanks; i++) {
            if (listOfNetAmounts[i].netAmount == 0 || listOfNetAmounts[i].netAmount < 0) continue;

            Set<String> intersection = new HashSet<>(listOfNetAmounts[minIndex].types);
            intersection.retainAll(listOfNetAmounts[i].types);

            if (!intersection.isEmpty() && max < listOfNetAmounts[i].netAmount) {
                max = listOfNetAmounts[i].netAmount;
                maxIndex = i;
                matchingType = intersection.iterator().next();
            }
        }

        return new Pair<>(maxIndex, matchingType);
    }

    private static void printAns(List<List<Pair<Integer, String>>> ansGraph, int numBanks, Bank[] input) {
        System.out.println("\nThe transactions for minimum cash flow are as follows : \n");
        for (int i = 0; i < numBanks; i++) {
            for (int j = 0; j < numBanks; j++) {
                if (i == j) continue;

                if (ansGraph.get(i).get(j).getFirst() != 0 && ansGraph.get(j).get(i).getFirst() != 0) {
                    if (ansGraph.get(i).get(j).getFirst().equals(ansGraph.get(j).get(i).getFirst())) {
                        ansGraph.get(i).set(j, new Pair<>(0, ""));
                        ansGraph.get(j).set(i, new Pair<>(0, ""));
                    } else if (ansGraph.get(i).get(j).getFirst() > ansGraph.get(j).get(i).getFirst()) {
                        int diff = ansGraph.get(i).get(j).getFirst() - ansGraph.get(j).get(i).getFirst();
                        ansGraph.get(i).set(j, new Pair<>(diff, ansGraph.get(i).get(j).getSecond()));
                        ansGraph.get(j).set(i, new Pair<>(0, ""));
                        System.out.println(input[i].name + " pays Rs " + diff + " to " + input[j].name + " via " + ansGraph.get(i).get(j).getSecond());
                    } else {
                        int diff = ansGraph.get(j).get(i).getFirst() - ansGraph.get(i).get(j).getFirst();
                        ansGraph.get(j).set(i, new Pair<>(diff, ansGraph.get(j).get(i).getSecond()));
                        ansGraph.get(i).set(j, new Pair<>(0, ""));
                        System.out.println(input[j].name + " pays Rs " + diff + " to " + input[i].name + " via " + ansGraph.get(j).get(i).getSecond());
                    }
                } else if (ansGraph.get(i).get(j).getFirst() != 0) {
                    System.out.println(input[i].name + " pays Rs " + ansGraph.get(i).get(j).getFirst() + " to " + input[j].name + " via " + ansGraph.get(i).get(j).getSecond());
                } else if (ansGraph.get(j).get(i).getFirst() != 0) {
                    System.out.println(input[j].name + " pays Rs " + ansGraph.get(j).get(i).getFirst() + " to " + input[i].name + " via " + ansGraph.get(j).get(i).getSecond());
                }
            }
        }
        System.out.println();
    }

    private static void minimizeCashFlow(int numBanks, Bank[] input, Map<String, Integer> indexOf, int numTransactions, int[][] graph, int maxNumTypes) {
        Bank[] listOfNetAmounts = new Bank[numBanks];
        for (int b = 0; b < numBanks; b++) {
            listOfNetAmounts[b] = new Bank(input[b].name, input[b].types);
            int amount = 0;
            for (int i = 0; i < numBanks; i++) {
                amount += graph[i][b];
            }
            for (int j = 0; j < numBanks; j++) {
                amount -= graph[b][j];
            }
            listOfNetAmounts[b].netAmount = amount;
        }

        List<List<Pair<Integer, String>>> ansGraph = new ArrayList<>();
        for (int i = 0; i < numBanks; i++) {
            ansGraph.add(new ArrayList<>());
            for (int j = 0; j < numBanks; j++) {
                ansGraph.get(i).add(new Pair<>(0, ""));
            }
        }

        int numZeroNetAmounts = 0;
        for (int i = 0; i < numBanks; i++) {
            if (listOfNetAmounts[i].netAmount == 0) numZeroNetAmounts++;
        }
        while (numZeroNetAmounts != numBanks) {
            int minIndex = getMinIndex(listOfNetAmounts, numBanks);
            Pair<Integer, String> maxAns = getMaxIndex(listOfNetAmounts, numBanks, minIndex, input, maxNumTypes);
            int maxIndex = maxAns.getFirst();
            if (maxIndex == -1) {
                ansGraph.get(minIndex).set(0, new Pair<>(Math.abs(listOfNetAmounts[minIndex].netAmount), input[minIndex].types.iterator().next()));
                int simpleMaxIndex = getSimpleMaxIndex(listOfNetAmounts, numBanks);
                ansGraph.get(0).set(simpleMaxIndex, new Pair<>(Math.abs(listOfNetAmounts[minIndex].netAmount), input[simpleMaxIndex].types.iterator().next()));
                listOfNetAmounts[simpleMaxIndex].netAmount += listOfNetAmounts[minIndex].netAmount;
                listOfNetAmounts[minIndex].netAmount = 0;
                if (listOfNetAmounts[minIndex].netAmount == 0) numZeroNetAmounts++;
                if (listOfNetAmounts[simpleMaxIndex].netAmount == 0) numZeroNetAmounts++;
            } else {
                int transactionAmount = Math.min(Math.abs(listOfNetAmounts[minIndex].netAmount), listOfNetAmounts[maxIndex].netAmount);
                ansGraph.get(minIndex).set(maxIndex, new Pair<>(transactionAmount, maxAns.getSecond()));
                listOfNetAmounts[minIndex].netAmount += transactionAmount;
                listOfNetAmounts[maxIndex].netAmount -= transactionAmount;
                if (listOfNetAmounts[minIndex].netAmount == 0) numZeroNetAmounts++;
                if (listOfNetAmounts[maxIndex].netAmount == 0) numZeroNetAmounts++;
            }
        }

        printAns(ansGraph, numBanks, input);
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("\n\t\t\t\t********************* Welcome to CASH FLOW MINIMIZER SYSTEM ***********************\n\n\n");
        System.out.println("This system minimizes the number of transactions among multiple banks in the different corners of the world that use different modes of payment. There is one world bank (with all payment modes) to act as an intermediary between banks that have no common mode of payment. \n");
        System.out.println("Enter the number of banks participating in the transactions.");
        int numBanks = sc.nextInt();

        Bank[] input = new Bank[numBanks];
        Map<String, Integer> indexOf = new HashMap<>();

        System.out.println("Enter the details of the banks and transactions as stated:");
        System.out.println("Bank name, number of payment modes it has and the payment modes.");
        System.out.println("Bank name and payment modes should not contain spaces");

        int maxNumTypes = 0;
        for (int i = 0; i < numBanks; i++) {
            if (i == 0) {
                System.out.print("World Bank : ");
            } else {
                System
