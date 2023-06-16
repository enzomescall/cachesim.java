// take in the name of the file holding the load and store commands

// take in cache configuration size, associativity, and type (wb or wt)

// take in block size in bytes


import java.io.File;

import java.io.FileNotFoundException;

import java.util.*;


public class cachesim {

    // the basic information inputted in the command line

    static String FILE;

    static int size;

    static int associativity;

    static boolean isWB;

    static int block;

    static char[] memory = new char[131072]; // multiplying by 2 because 2 char = 1 pseudo-byte


    // ArrayList where each line is an array with the information in the file

    static ArrayList<String[]> fileLines = new ArrayList<>();


    public static void readFile(String path) {

        try {

            File text = new File(path);

            Scanner myReader = new Scanner(text);

            while (myReader.hasNextLine()) {

                String data = myReader.nextLine();

                // here we will split the line into the its useful parts

                // s[0] is load or store

                // s[1] is the memory address

                // s[2] is the size of access

                // s[3] is the value to be written if it exists

                String[] s = data.split(" ");


                // adding this to the fileLines arraylist

                fileLines.add(s);

            }

            myReader.close();

        } catch(FileNotFoundException e) {

            // PRINT STATEMENT System.out.println("oopsie woopsie! it seems like something went wrong! uwu");

            e.printStackTrace();

        }

    }


    public static class cacheBlock {

        private boolean dirty;

        private int tag;

        private char[] data;


        public boolean sameBlock(cacheBlock other) {

            return this.tag == other.tag;

        }


        public void writeToMemory() {

            if (this.data == null) {

                System.out.println("(method writeToMemory) THIS BLOCK " + this.tag + " HAS NULL DATA");

                return;

            }

            // block tag represents its first location in memory

            // this should loop through all of data

            // and then write to memory as it does that

            // PRINT STATEMENT System.out.print("Writing data to memory: ");

            // PRINT STATEMENT System.out.println(this.data);

            for (int i = 0; i < this.data.length; i++) {

                memory[2*this.tag + i] = this.data[i];

            }

        }


        public void writeToBlock(char[] data, int quantity, boolean dirty, int address) {

            // set dirty if dirty == true

            this.dirty = dirty;

            if (this.data == null) {

                this.data = new char[block];

                for (int i = 0; i < block; i++) {

                    this.data[i] = '0';

                }

            }


            // calculate the relative address


            int relativeAddress = address - this.tag;

            if (quantity > data.length) quantity = data.length;

            for (int i = 0; i < data.length && i + 2*relativeAddress < this.data.length; i++) {

                this.data[i + 2*relativeAddress] = data[i];

            }


        }

    }


    public static class set {

        private Queue<cacheBlock> blocks;


        // constructor

        public set() {

            blocks = new LinkedList<>();

        }


        public void printQueue(Queue<cacheBlock> queue) {

            // iterate through queue and print it all

            Queue<cacheBlock> q = new LinkedList<>(queue);

            System.out.print("Printing Queue of size " + q.size() + ":");

            while(q.size() > 0) {

                cacheBlock blockToCheck = q.remove();

                System.out.print(" " + blockToCheck.tag);

            }

            System.out.println(" done");

        }


        public boolean hasBlock(cacheBlock block) {

            // copy the queue blocks

            Queue<cacheBlock> copy = new LinkedList<>(blocks);

            int copySize = copy.size();

            for (int i = 0; i < copySize; i++) {

                // pop the first cacheBlock

                cacheBlock temp = copy.remove();

                // PRINT STATEMENT System.out.print("(method hasBlock) checking equal " + temp.tag + " == " + block.tag);

                // determine if this cacheBlock contains the address

                if (temp.tag == block.tag) {

                    // PRINT STATEMENT System.out.println(" true");

                    return true;

                } else {

                    // PRINT STATEMENT System.out.println(" false");

                }

            }

            return false;

        }


        public void insert(cacheBlock block) {

            if (blocks.size() >= associativity) {

                cacheBlock dirtyCheck = blocks.remove();

                if (dirtyCheck.dirty) {

                    // PRINT STATEMENT System.out.println("dirty block found! " + dirtyCheck.tag);

                    dirtyCheck.writeToMemory();

                }

            }

            blocks.add(block);

            // PRINT STATEMENT System.out.print("(method insert) Confirming adding " + block.tag + " with data ");

            // PRINT STATEMENT System.out.println(block.data);

        }


        public void reorder(cacheBlock block) {

            // we will iterate through the queue to find the current hit

            Queue<cacheBlock> tempQueue = new LinkedList<>();


            // this is the block we will copy into once we find our target

            cacheBlock targetBlock = new cacheBlock();


            // PRINT STATEMENT System.out.println("(method reorder) with: " + block.tag + " and size: " + blocks.size());

            while(blocks.size() > 0) {

                cacheBlock blockToCheck = blocks.remove();

                // PRINT STATEMENT System.out.print("currently checking: " + blockToCheck.tag + " with data: ");

                // PRINT STATEMENT System.out.println(blockToCheck.data);

                if(blockToCheck.sameBlock(block)) {

                    // PRINT STATEMENT System.out.println("block " + blockToCheck.tag + " found!" + blockToCheck.data[0]);

                    targetBlock = blockToCheck;

                } else {

                    tempQueue.add(blockToCheck);

                }

            }

            // ...and then add it back at the top

            tempQueue.add(targetBlock);

            // PRINT STATEMENT System.out.println("adding " + block.tag + " back on top" + targetBlock.data[0]);


            // adding copy back to blocks

            blocks.addAll(tempQueue);

            // printQueue(blocks);

        }


        public String findAddress(int myAddress, int myBlockAddress, int mySize) {

            // copy the queue blocks

            Queue<cacheBlock> copy = new LinkedList<>(blocks);

            int relativeAddress = myAddress - myBlockAddress;


            for (int i = 0; i < associativity; i++) {

                // pop the first cacheBlock

                cacheBlock temp = copy.remove();

                // PRINT STATEMENT System.out.print("(method findAddress) pop block " + temp.tag + " with data ");

                // PRINT STATEMENT System.out.println(temp.data);

                // determine if this cacheBlock contains the address

                if (temp.tag == myBlockAddress) {

                    // pull out the right part of the block

                    char[] dataCopy = Arrays.copyOfRange(temp.data, 2*relativeAddress, 2*(relativeAddress + mySize)); // multiplying by 2 because 2 char = 1 pseudo-byte


                    // PRINT STATEMENT System.out.println("data: " + temp.data[0] + " found at address: " + relativeAddress);

                    return String.copyValueOf(dataCopy);

                }

            }


            return "error uwu find address inconclusive";

        }


        public cacheBlock popBlock(cacheBlock block) {

            // we will iterate through the queue to find the current hit

            Queue<cacheBlock> tempQueue = new LinkedList<>();


            // this is the block we will pop

            cacheBlock targetBlock = new cacheBlock();


            // PRINT STATEMENT System.out.println("(method popBlock) with: " + block.tag + " and size: " + blocks.size());

            while(blocks.size() > 0) {

                cacheBlock blockToCheck = blocks.remove();

                // PRINT STATEMENT System.out.print("currently checking: " + blockToCheck.tag + " with data: ");

                // PRINT STATEMENT System.out.println(blockToCheck.data);

                if(blockToCheck.sameBlock(block)) {

                    // PRINT STATEMENT System.out.println("block " + blockToCheck.tag + " found!" + blockToCheck.data[0]);

                    targetBlock = blockToCheck;

                } else {

                    tempQueue.add(blockToCheck);

                }

            }

            // PRINT STATEMENT System.out.println("adding " + block.tag + " back on top" + targetBlock.data[0]);


            // adding copy back to blocks

            blocks.addAll(tempQueue);

            // printQueue(blocks);

            return targetBlock;

        }

    }



    public static class Cache {

        private static set[] sets;


        public Cache(int setQuantity) {

            set[] newSets = new set[setQuantity];

            for (int i = 0; i < setQuantity; i++) {

                newSets[i] = new set();

            }

            this.sets = newSets;

        }


        // this will be what does the loads/stores

        // s[0] is load or store

        // s[1] is the memory address

        // s[2] is the size of access

        // s[3] is the value to be written if it exists

        public static String lineCommand(String[] lineInfo) {

            // creating new cache block to see if it exists

            cacheBlock tempBlock = new cacheBlock();


            // aligning address to block sized intervals

            int address = Integer.parseInt(lineInfo[1], 16);

            int blockAddress = address - (address%block);

            tempBlock.tag = blockAddress;


            // calculate which set the block would fall into

            // formula is (address/sett quantity)%set quantity

            int setNumber = (blockAddress/block)%(sets.length);


            // PRINT STATEMENT System.out.println ("looking at: (" + blockAddress + " / " + block + ") % " + sets.length + " = " + setNumber);

            set currentSet = sets[setNumber];


            // check if this block exists in that set

            boolean hit = currentSet.hasBlock(tempBlock);


            // deciphering whether load or store command

            if (lineInfo[0].equals("load")) {

                // implementing loading word functionality

                if (hit) {

                    // go into the sets[] and find which block

                    // get that block's data

                    String data0 = currentSet.findAddress(address, blockAddress, Integer.parseInt(lineInfo[2]));


                    // make sure this block is on top of set's queue

                    currentSet.reorder(tempBlock);


                    return lineInfo[0] + " " + lineInfo[1] + " hit " + data0;

                } else { // miss

                    // accessing data from memory and adding it to block

                    char[] tempBlockData = Arrays.copyOfRange(memory, 2*blockAddress, 2*(blockAddress + block)); // multiplying by 2 because 2 char = 1 pseudo-byte

                    tempBlock.data = tempBlockData;


                    // adding the block into the cache

                    currentSet.insert(tempBlock);

                    // PRINT STATEMENT System.out.print("missed load, bringing block " + tempBlock.tag + " from cache with data: ");

                    // PRINT STATEMENT System.out.println(tempBlock.data);


                    // pulling data from the block at the accurate address (done by set)

                    String data1 = currentSet.findAddress(address, blockAddress, Integer.parseInt(lineInfo[2]));


                    // returning in the specified fashion

                    return lineInfo[0] + " " + lineInfo[1] + " miss " + data1;

                }

            } else {

                // implementing store functionality

                // differentiate between wt and wb

                if (isWB) {

                    // write-back, write-allocate case

                    // just add dirty block to cache

                    // if miss, pull block to cache and write to it


                    if (hit) {

                        // add data to the block

                        // somehow find block

                        // pop block from set, write to block, add back to set

                        cacheBlock popBlock = currentSet.popBlock(tempBlock);

                        popBlock.writeToBlock(lineInfo[3].toCharArray(), Integer.parseInt(lineInfo[2]), true, address);


                        // inserting it at the top of the set

                        currentSet.insert(popBlock);


                        return lineInfo[0] + " " + lineInfo[1] + " hit";

                    } else { // miss

                        // read block from lower level

                        char[] tempBlockData = Arrays.copyOfRange(memory, 2*blockAddress, 2*(blockAddress + block)); // multiplying by 2 because 2 char = 1 pseudo-byte

                        tempBlock.data = tempBlockData;


                        // write value into it

                        tempBlock.writeToBlock(lineInfo[3].toCharArray(), Integer.parseInt(lineInfo[2]), true, address);

                        // insert into cache

                        currentSet.insert(tempBlock);


                        return lineInfo[0] + " " + lineInfo[1] + " miss";

                    }


                } else { // is WT

                    // write-through, write-no-allocate case

                    // immediately write to data

                    // if miss, just write do data and don't pull to cache


                    if (hit) {

                        // add data to the block

                        cacheBlock popBlock = currentSet.popBlock(tempBlock);

                        popBlock.writeToBlock(lineInfo[3].toCharArray(), Integer.parseInt(lineInfo[2]), false, address);


                        // inserting it at the top of the set

                        currentSet.insert(popBlock);


                        // and also write directly to memory

                        // write directly to data

                        popBlock.writeToMemory();


                        return lineInfo[0] + " " + lineInfo[1] + " hit";

                    } else { // miss

                        // write directly to memory
                        // todo make sure you only write the relevant piece
                        for (int i = 0; i < 2*Integer.parseInt(lineInfo[2]); i++) {
                            memory[i + 2*address] = lineInfo[3].toCharArray()[i];
                        }

                        return lineInfo[0] + " " + lineInfo[1] + " miss";

                    }

                }

            }

        }

    }


    public static void main(String[] args) {

        // making sure memory is 0 lol

        for (int i = 0; i < memory.length; i++) memory[i] = '0';


        // assigning all command line info

        FILE = args[0];

        size = Integer.parseInt(args[1]);

        associativity = Integer.parseInt(args[2]);

        isWB = args[3].equals("wb");

        block = Integer.parseInt(args[4]);


        // basic extrapolation from inputted information

        int setQuantity = (size * 1024)/(associativity * block);


        // file contents will be in fileLines arraylist

        readFile(FILE);


        // initialise cache

        Cache main = new Cache(setQuantity);

        // loop through arguments

        for (int i = 0; i < fileLines.size(); i++){

            // pulling the lines one by one

            String[] currentLine = fileLines.get(i);

            // outputting the results one by one

            System.out.println(main.lineCommand(currentLine));

        }

    }

}
