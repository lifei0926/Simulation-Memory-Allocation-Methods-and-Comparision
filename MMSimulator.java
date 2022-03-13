import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// Memory Management Simulator
public class MMSimulator {
	static int numOfProcess = 0;// will get data from input file
	static int timer = 0;// count from 0 to 409
	static int[][] arrProcess;// arrProcess[i] = {arrival_time, size_of_space, finishing_time}
	static int[][] arrMemory;// arrMemory[i] = {size_of_space, finishing_time}
	static String input = "";
	static int sumOfInternalFragmentation = 0;
	static int sumOfExternalFragmentation = 0;
	static int numOfAllocationFailed = 0;
	static int sumOfCompaction = 0;	
	static int sumOfMemoryUsed = 0;
	static int runner = 0;
	public static void main(String[] args) {
		//*****************************************************************************
		// read data from input file and store it in an array
		//*****************************************************************************
		try {		
			File file = new File("input_MemoryAllocation.txt");
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);
			numOfProcess = Integer.valueOf(br.readLine());
			arrProcess = new int[numOfProcess][3];
			String[] arrTemp = new String[4];//we have 4 elements each line from the 2nd line
			while((input = br.readLine()) != null && !input.isEmpty()) {
				arrTemp = input.split(" ",0);
				arrProcess[Integer.valueOf(arrTemp[0])-1][0] = Integer.valueOf(arrTemp[1]);
				arrProcess[Integer.valueOf(arrTemp[0])-1][1] = Integer.valueOf(arrTemp[2]);
				arrProcess[Integer.valueOf(arrTemp[0])-1][2] = Integer.valueOf(arrTemp[3]);
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//**************************The End*********************************************
		
		
		
		//******************************************************************************
		//Fixed Partition with Equal Size (FE)
		//******************************************************************************
		// set up memory for fixed partition with equal size (FE)
		int[][] FE = new int[7][2];// the first 8MB was allocated to the OS, so we have only 7 blocks left 
		for(int i = 0; i < 7; ++i) {
				FE[i][0] = 8;// 8MB for each page frame
				FE[i][1] = 0;// no process is using this frame
			}
		// simulation starts
		// reset counters
		int numOfExternalFragmentation = 7;
		sumOfExternalFragmentation = 0;
		sumOfInternalFragmentation = 0;
		numOfAllocationFailed = 0;	
		sumOfMemoryUsed = 0;
		timer = 0;
		int actualRunningTime = 0;
		//**************************Time to rock*****************************************
		while(timer < 410) {// keep tracks of the time
			// deallocate memory when time is up
			for(int i = 0; i < 7; ++i) {
				if(FE[i][1] == timer && timer != 0) {
					FE[i][1] = 0;//reset finishing time and free space
					actualRunningTime = timer;
					numOfExternalFragmentation++;
				}
			}
			
			// allocate memory at current time
			for(int i = 0; i < arrProcess.length; ++i) {
				if(arrProcess[i][0] == timer) {//find the process requesting the memory
					int internalFrag = 8 - arrProcess[i][1];
					if(internalFrag < 0) numOfAllocationFailed ++;
					else {// try to find a spot
						runner = 0;// reset to 0 to traverse the memory locations
						while(runner < 7 && FE[runner][1] != 0)// look for an available spot
								runner++;
						if(runner != 7){// not out of bond
							FE[runner][1] = arrProcess[i][2];
							// internal fragmentation * its duration time
							sumOfInternalFragmentation += internalFrag * (arrProcess[i][2] - timer);
							// memory used * its duration time
							sumOfMemoryUsed += arrProcess[i][1] * (arrProcess[i][2] - timer);
							numOfExternalFragmentation--;
						}else// out of bond
							numOfAllocationFailed++;
					}
				}
			}
			sumOfExternalFragmentation += numOfExternalFragmentation*8;
			timer++;
		}
		if(actualRunningTime != 0) {
			System.out.println("Fixed Partition with Equal Size (FE): ");
			System.out.println("Average Internal Fragmentation is " + sumOfInternalFragmentation / actualRunningTime);
			System.out.println("Average External Fragmentation is " + sumOfExternalFragmentation / actualRunningTime);
			System.out.println("The Number of allocation fails is " + numOfAllocationFailed);
			System.out.println("Memory Utilization is " + (double)(sumOfMemoryUsed + 8 * actualRunningTime) / (64 * actualRunningTime) * 100 + "%");
			System.out.println();
		}
		//***************************The End******************************************
		
		
		
		//****************************************************************************
		// Fixed Partition with Unequal Size (FU)
		//****************************************************************************
		// set up memory for fixed partition with unequal size (FU)
		int[][] FU = new int[11][2];
		FU[0][0] = 2;
		FU[1][0] = 2;
		FU[2][0] = 4;
		FU[3][0] = 4;
		FU[4][0] = 4;
		FU[5][0] = 4;
		FU[6][0] = 6;
		FU[7][0] = 6;
		FU[8][0] = 8;
		FU[9][0] = 8;
		FU[10][0] = 8;
		for(int i = 0; i < 11; ++i)
			FU[i][1] = 0;// finishing time preset to 0
		// simulation starts
		// reset counters

		sumOfExternalFragmentation = 0;
		sumOfInternalFragmentation = 0;
		numOfAllocationFailed = 0;	
		sumOfMemoryUsed = 0;
		timer = 0;
		actualRunningTime = 0;
		//**************************Time to rock*****************************************
		while(timer < 410) {// keep tracks of the time
			// deallocate memory when time is up
			for(int i = 0; i < 11; ++i) {
				if(FU[i][1] == timer) {
					FU[i][1] = 0;//reset finishing time and free space
					actualRunningTime = timer;// keep updating with the recent finishing time of processes
				}
			}
			
			// allocate memory at current time
			for(int i = 0; i < arrProcess.length; ++i) {
				if(arrProcess[i][0] == timer) {//find the process requesting the memory
					
					// try to find a spot for the process
					runner = 0;// reset to 0 to traverse the memory locations
					// When the runner is still in the range, we have to guarantee that the spot is not occupied or too small. 
					// Otherwise, we have to check the next spot
					while(runner < 11 && (FU[runner][1] != 0 || FU[runner][0] < arrProcess[i][1]))// look for an available spot
							runner++;
					if(runner != 11){// not out of bond and that means we didn't find a spot for the current process
						FU[runner][1] = arrProcess[i][2];
						// internal fragmentation * its duration time
						sumOfInternalFragmentation += (FU[runner][0] - arrProcess[i][1]) * (arrProcess[i][2] - timer);
						// memory used * its duration time
						sumOfMemoryUsed += arrProcess[i][1] * (arrProcess[i][2] - timer);
					}else// out of bond
						numOfAllocationFailed++;
					
				}
			}
			for(int j = 0; j< FU.length; ++j) {//calculate external fragmentation
				if(FU[j][1] == 0) {
					sumOfExternalFragmentation += FU[j][0];
				}
			}
			timer++;
		}
		if(actualRunningTime != 0) {
			System.out.println("Fixed Partition with Uequal Size (FU): ");
			System.out.println("Average Internal Fragmentation is " + sumOfInternalFragmentation / actualRunningTime);
			System.out.println("Average External Fragmentation is " + sumOfExternalFragmentation / actualRunningTime);
			System.out.println("The Number of allocation fails is " + numOfAllocationFailed);
			System.out.println("Memory Utilization is " + (double)(sumOfMemoryUsed + 8 * actualRunningTime) / (64 * actualRunningTime) * 100 + "%");
			System.out.println();
		}
		//*****************************The End*******************************************
		
		
		
		//*******************************************************************************
		// Dynamic Partition with First Fit (DFF)
		//*******************************************************************************
		// set up memory for dynamic partition with first fit
		List<int[]> DFF = new ArrayList<>();
		// The other 8 MB are used for the OS
		DFF.add(newMemory(56,0));// initialize 56 MB space
		int externalFrag = 56;
		
		// simulation starts
		// reset counters
		sumOfExternalFragmentation = 0;
		numOfAllocationFailed = 0;	
		sumOfMemoryUsed = 0;
		sumOfCompaction = 0;	
		timer = 0;
		actualRunningTime = 0;
		//**************************Time to rock*****************************************
		while(timer < 410) {// keep tracks of the time
			// deallocate memory when time is up
			// update external fragmentation
			//update the actual running time
			for(int i = 0; i < DFF.size(); ++i) {
				if(DFF.get(i)[1] == timer && timer != 0) {
					DFF.get(i)[1] = 0;
					externalFrag += DFF.get(i)[0];
					actualRunningTime = timer;// update the actual running time
				}
			}
				
			// allocate memory at current time
			for(int i = 0; i < arrProcess.length; ++i) {
				if(arrProcess[i][0] == timer) {//find the process requesting the memory
					// check whether the external fragmentation is enough
					if(externalFrag < arrProcess[i][1]) // if the external memory is less than the process required, allocation fails immediately
						numOfAllocationFailed++;
					else {
						// case 1: we don't have to compact the remaining memory
						
						//new codes added--->
						int slowRunner = 0;
						runner = slowRunner + 1;
						int sum = 0;
						while(runner != DFF.size() ) {
							while(runner != DFF.size() && (DFF.get(slowRunner)[1] != 0 || DFF.get(runner)[1] != 0)) {// find the next two slots to combine
								slowRunner++;
								runner++;
							}
							sum = 0;// reset to 0
							// calculate the combined size
							while(runner != DFF.size() && DFF.get(slowRunner)[1] == 0 && DFF.get(runner)[1] == 0) {
								sum += DFF.get(slowRunner)[0];
								slowRunner++;
								runner++;
							}
							sum += DFF.get(slowRunner)[0];
							DFF.add(runner,newMemory(sum, 0));//add memory
							do {//delete memory; add and delete are doing a replacement
								DFF.remove(slowRunner);
								slowRunner--;
							}while(slowRunner > -1 && DFF.get(slowRunner)[1] == 0);
							
							runner = slowRunner + 1;
							slowRunner++;
							runner++;
						}
								
						//<---new codes added
						
						
						// try to find a slot for the process
						// reset runner to 0
						runner = 0;
						// find the slot by First Fit
						while(runner < DFF.size() && (DFF.get(runner)[0] < arrProcess[i][1] || DFF.get(runner)[1] != 0)) {// find a space for the process
							runner++;
						}
							
						if(runner != DFF.size()) {// Once found an available position, fit in the process
							//System.out.println("Runner" + runner + "/" +DFF.size());// for testing
							DFF.add(runner, newMemory(DFF.get(runner)[0] - arrProcess[i][1], 0));
							DFF.add(runner, newMemory(arrProcess[i][1], arrProcess[i][2]));
							DFF.remove(runner+2);
							externalFrag -= arrProcess[i][1];// update the external fragmentation
							sumOfMemoryUsed += arrProcess[i][1] * (arrProcess[i][2] - arrProcess[i][0]);
						}else {// case 2: try to compact external fragmentation slots
								for(int j = 0; j < DFF.size(); ++j) {// COMPACTION
									if(DFF.get(j)[1] == 0) {
										DFF.remove(j);
										j--;
									}
								}
								DFF.add(newMemory(arrProcess[i][1], arrProcess[i][2]));
								sumOfMemoryUsed += arrProcess[i][1] * (arrProcess[i][2] - arrProcess[i][0]);
								sumOfCompaction++;
								externalFrag -= arrProcess[i][1];
							}
							
					}
				}
			}
			timer++;
		}
		if(actualRunningTime != 0) {
			System.out.println("Dynamic Partition with First Fit (DFF): ");
			System.out.println("Average External Fragmentation is " + (56*actualRunningTime - sumOfMemoryUsed) / actualRunningTime);
			System.out.println("The Number of Compaction for Dynamic Partition is " + sumOfCompaction);
			System.out.println("The Number of Allocation fails is " + numOfAllocationFailed);
			System.out.println("Memory Utilization is " + (double)(sumOfMemoryUsed + 8 * actualRunningTime) / (64 * actualRunningTime) * 100 + "%");
			System.out.println();
			
		}
		//******************************The End*******************************************
	}
		//helper method
		// create new memory space for dynamic partition
		public static int[] newMemory(int size, int finishingTime){
			int[] temp = new int[2];
			// temp[0] is the memory size and temp[1] is the finishing time.
			temp[0] = size;
			temp[1] = finishingTime;
			return temp;
		}

	
}
