
package osproject2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.Exception;
import java.util.ArrayList;

/**
 *
 * @author Fatemeh
 */
public class Main {

  private static Integer numOfFrameInMem;
    private static Integer sizeOfProcess;
    private static Integer sizeOfPage;
    private static String replaceAlgo;
    private static Integer numOfTlbRow;
    private static ArrayList<op> operations = new ArrayList<op>();
    private static Integer numOfLogicalMemPage;
    private static int[] logicalMem;
    private static PhysicalMem[] physicalMem;
    private static TLB_S[] tlb;
    private static int[] tlb_counter;
    private static PT_S[] pt;
    private static int[] tagIndex;
    private static boolean [] isDirty;
    private static int counter = 0;
    private static Integer[] refrence;
    private static Integer[] refPointer = new Integer[1];
    private static boolean[]refrenceFlag = new boolean[4];
    private static Integer fifoPinter = 0;
    private static PrintWriter pw;
    private static Exception Exception;
    
    
    public static void main(String[] args) throws FileNotFoundException, IOException, Exception {

        makeRedy();
        System.out.println("----------------------------");
        inital();
         for (int i = 0;i<operations.size();i++)
         {
             System.out.println("# i = "+i);
                pw.println("# i = "+i);
                op request = operations.get(i);
                System.out.println("# page = " + request.page);
                pw.println("# page = " + request.page);
//protection
                if(!pageAddrProtection(numOfLogicalMemPage,request.page))
                {
                    System.out.println("-------------------------------------");
                    pw.println("-------------------------------------");
                    continue;
                }
                if(!pageOffsetProtection(sizeOfPage,request.offset))
                {
                    System.out.println("-------------------------------------");
                    pw.println("-------------------------------------");
                    continue;
                }
                int find = searchTLB(request.page);// search and manage counter if it is found
//check
                System.out.println("###-------------------------------------");
                pw.println("###-------------------------------------");
                for (int j = 0;j<tlb.length;j++)
                {
                    System.out.println(tlb[j].logicalAddr + "   "+ tlb[j].physicalAddr+"    "+tlb_counter[j]);
                    pw.println(tlb[j].logicalAddr + "   "+ tlb[j].physicalAddr+"    "+tlb_counter[j]);
                }
                System.out.println("###-------------------------------------");
                pw.println("###-------------------------------------");
///
                if(find!=-1) //find in tlb
                {
                    System.out.println("page found in TBL is in frame "+ find);
                    pw.println("page found in TBL is in frame "+ find);
                    //for lru algo
                    //update tag index
                    tagIndex[find] = counter;
                    counter ++;
                    //
                    //fot second chanse
                    setRefrence(find, request.type);
                    if(request.type.equals("w"))
                    {
                        isDirty[find] = true;
                        System.out.println("data is  modified.");
                        pw.println("data is  modified.");
                    }
                }

                else// not found in tlb search in pt and then refresh th tlb
                {
                    System.out.println("page not found in TBL search PT ");
                    pw.println("page not found in TBL search PT ");

                    if(pt[request.page].valid) //find in physical mem
                    {
                        System.out.println("page is in physical mem");
                        pw.println("page is in physical mem");
                        //for lru
                        tagIndex[pt[request.page].physicalAddr] = counter;
                        counter ++;
                        //
                        //for second chanse
                        setRefrence(pt[request.page].physicalAddr,request.type);
                        //
                        System.out.print("update tlb: ");
                        pw.print("update tlb: ");
                        updateTLB(request.page,pt[request.page].physicalAddr);
                        if(request.type.equals("w"))
                        {
                            isDirty[pt[request.page].physicalAddr] = true;
                            System.out.println("data is  modified.");
                            pw.println("data is  modified.");
                        }
                    }
                    else // not found in physical mem
                    {
                        int freeFrameIndex = findFreeFrame();
                        if (freeFrameIndex!=-1) //has free frame
                        {
                            // swup in that page
                            System.out.println("copy page "+ request.page + " from vitual to physical memory in "+ freeFrameIndex);
                            pw.println("copy page "+ request.page + " from vitual to physical memory in "+ freeFrameIndex);
                            //frame is full
                            physicalMem[freeFrameIndex].isfull = 1;
                            physicalMem[freeFrameIndex].val = request.page;

                            //update_pt

                            System.out.println("update pt, row "+ request.page + ", validation is true");
                            pw.println("update pt, row "+ request.page + ", validation is true");
                            pt[request.page].valid = true;
                            System.out.println("update pt, row "+ request.page + ", frame is "+ freeFrameIndex);
                            pw.println("update pt, row "+ request.page + ", frame is "+ freeFrameIndex);
                            pt[request.page].physicalAddr = freeFrameIndex;

                            //update tag index
                            tagIndex[freeFrameIndex] = counter;
                            counter ++;
                            //
                            //for second chance
                            setRefrence(freeFrameIndex,request.type);

                            //update tlb
                            System.out.print("update tlb: ");
                            pw.print("update tlb: ");
                            updateTLB(request.page,pt[request.page].physicalAddr);
                            if(request.type.equals("w"))
                            {
                                isDirty[pt[request.page].physicalAddr] = true;
                                System.out.println("data is  modified.");
                                pw.println("data is  modified.");
                            }
                        }
                        else
                        {
                            if(replaceAlgo.equals("fifo"))
                            {
                                System.out.println("free frame not found, wait instruction, use FIFO algorithm");
                                pw.println("free frame not found, wait instruction,use FIFO algorithm");
                                 //find free frame and swuap out
                                freeFrameIndex = fifoPinter;
                                fifoPinter = (fifoPinter+1)%physicalMem.length;
                            }
                            else if (replaceAlgo.equals("lru"))
                            {
                                System.out.println("free frame not found, wait instruction, use LRU algorithm");
                                pw.println("free frame not found, wait instruction,use LRU algorithm");
                                freeFrameIndex = Algo.LRU(tagIndex);
                            }
                            else if (replaceAlgo.equals("opt"))
                            {
                                System.out.println("free frame not found, wait instruction, use OPT algorithm");
                                pw.println("free frame not found, wait instruction,use OPT algorithm");
                             //find free frame and swuap out
                                freeFrameIndex = Algo.OPT(operations, i, physicalMem, pt);

                            }
                            else if (replaceAlgo.equals("secondchanse"))
                            {
                                System.out.println("free frame not found, use secondchanse algorithm");
                                pw.println("free frame not found, use secondchanse algorithm");

                                System.out.println("#@"+refPointer[0]);
                                pw.println("#@"+refPointer[0]);
                                freeFrameIndex = Algo.secondChance(refPointer,refrence,refrenceFlag);
                                System.out.println("#@"+refPointer[0]);
                                pw.println("#@"+refPointer[0]);

                            }
                            else
                            {
                                throw Exception = new Exception("not found algo");
                            }

                        
                            System.out.println("condidate frame swap out is: "+ freeFrameIndex);
                            pw.println("condidate frame swap out is: "+ freeFrameIndex);
                            ///
                            if(isDirty[freeFrameIndex])
                            {
                                System.out.println("copy frame in logical mem becuse it is modified");
                                pw.println("copy frame in logical mem becuse it is modified");
                            }
                            else
                            {
                                System.out.println("not copy frame in logical mem becuse it is not modified");
                                pw.println("not copy frame in logical mem becuse it is not modified");
                            }
                            isDirty[freeFrameIndex] = false;
                            ///

                            //pt
                            System.out.println("update pt, row "+ physicalMem[freeFrameIndex].val + ", validation is false");
                            pw.println("update pt, row "+ physicalMem[freeFrameIndex].val + ", validation is false");
                            pt[physicalMem[freeFrameIndex].val].valid = false;
                            //tlb
                            int tempTLBPointer = deletTLBRow(physicalMem[freeFrameIndex].val);
                            if(tempTLBPointer != -1)
                            {
                                System.out.println("update TLB, row "+ tempTLBPointer + ", become free");
                                pw.println("update TLB, row "+ tempTLBPointer + ", become free");
                            }

                          //swap in
                            System.out.println("swap in requested page : "+ request.page);
                            pw.println("swap in requested page : "+ request.page);
                            //update pt
                            System.out.println("update pt, row "+ request.page + ", validation is true");
                            pw.println("update pt, row "+ request.page + ", validation is true");
                            pt[request.page].valid = true;
                            System.out.println("update pt, row "+ request.page + ", frame is "+ freeFrameIndex);
                            pw.println("update pt, row "+ request.page + ", frame is "+ freeFrameIndex);
                            pt[request.page].physicalAddr = freeFrameIndex;
                            //tlb
                            System.out.print("update tlb: ");
                            pw.print("update tlb: ");
                            int tempIndex = updateTLB(request.page, freeFrameIndex);
                            tlb_counter[tempIndex]--;

                            //physical mem
                            physicalMem[freeFrameIndex].val = request.page;

                            System.out.println("restart instruction");
                            pw.println("restart instruction");
                            i--;
                            System.out.println("-------------------------------------");
                            pw.println("-------------------------------------");
                            continue;
                        }
                    }
                }
                System.out.println("-------------------------------------");
                pw.println("-------------------------------------");

         }
         pw.close();
      
    }

    private static int deletTLBRow(int page) {
        for(int i =0;i<tlb.length;i++)
        {
            if (tlb[i].logicalAddr == page)
            {
                tlb_counter[i] = 0;
                return i;
            }
        }
        return -1; // not found
    }

    private static int findFreeFrame()
    {
        for(int i = 0;i<physicalMem.length;i++)
            if(physicalMem[i].isfull == 0)
                return i;
        return -1;
    }

    private static void inital()
    {
        numOfLogicalMemPage = sizeOfProcess/sizeOfPage;
        logicalMem = new int[numOfLogicalMemPage];
        pt = new PT_S[numOfLogicalMemPage];
        for(int i = 0;i<pt.length;i++)
        {
            pt[i] = new PT_S();
            pt[i].valid = false;
        }
        physicalMem = new PhysicalMem[numOfFrameInMem];
        for (int i = 0;i<numOfFrameInMem;i++)
        {
            physicalMem[i] = new PhysicalMem();
            physicalMem[i].isfull = 0;
            physicalMem[i].val = -1;

        }
        tagIndex = new int[physicalMem.length];
        for(int i = 0;i<tagIndex.length;i++)
            tagIndex[i] = -1;
        isDirty = new boolean[physicalMem.length];
        for(int i = 0;i<isDirty.length;i++)
            isDirty[i] = false ;
        tlb = new TLB_S[numOfTlbRow];
        for(int i = 0;i<tlb.length;i++)
        {
            tlb[i] = new TLB_S();
        }
        tlb_counter = new int[numOfTlbRow];
         for(int i = 0;i<tlb_counter.length;i++)
        {
            tlb_counter[i] = 0;
        }

        refrence = new Integer[physicalMem.length];
        for(int i = 0;i<refrence.length;i++)
            refrence[i] = 0;
        refPointer[0] = -1;
        for(int i = 0;i<refrenceFlag.length;i++)
               refrenceFlag[i] = false;
    }

    private static void makeRedy() throws FileNotFoundException, IOException
    {

         File inFile = new File("input.txt");
        BufferedReader br = new BufferedReader(new FileReader(inFile));
        pw = new PrintWriter("out.txt");
        String in = null;
        for (int i = 0;i<5;i++)
        {
            in = br.readLine();
            System.out.println(in);
            switch(i)
            {
                case 0:
                    numOfFrameInMem = Integer.parseInt(in);
                    break;
                case 1:
                    sizeOfProcess = Integer.parseInt(in);
                    break;
                case 2:
                    sizeOfPage = Integer.parseInt(in);
                    break;
                case 3:
                    replaceAlgo = in;
                    break;
                case 4:
                    numOfTlbRow = Integer.parseInt(in);
                    break;
            }
        }
         String opTemp[];
            while ((in = br.readLine()) != null)
            {
                opTemp = in.split(",");
                operations.add(new op(Integer.parseInt(opTemp[0]), Integer.parseInt(opTemp[1]), opTemp[2]));
            }
    }

    private static boolean pageAddrProtection(Integer numOfLogicalMemPage, int page)
    {
        if (page>numOfLogicalMemPage)
        {
            System.out.println("ERROR: page address is not valid");
            pw.println("ERROR: page address is not valid");
            return false;
        }
        System.out.println("CORRECT: page address is valid");
        pw.println("CORRECT: page address is valid");
        return true;
    }

     private static boolean pageOffsetProtection(Integer sizeOfPage, int offset)
    {
         if (offset>sizeOfPage)
        {
            System.out.println("ERROR: offset is not valid");
            pw.println("ERROR: offset is not valid");
            return false;
        }
         System.out.println("CORRECT: offset is valid");
         pw.println("CORRECT: offset is valid");
        return true;
    }

    private static int searchTLB(int page)  //return physical addr or -1 if not found
    {
        for(int i =0;i<tlb.length;i++)
        {
            if (tlb[i].logicalAddr == page)
            {
                tlb_counter[i]++;
                return tlb[i].physicalAddr;
            }
        }
        return -1; // not found
    }


    private static void setRefrence(int refrenseIndex, String type) {

        if(type.equals("w"))
        {
            switch(refrence[refrenseIndex])
            {
                case 0 :
                    refrence[refrenseIndex] = 2;

                    break;
                case 1:
                    refrence[refrenseIndex] = 3;
                    break;
                case 2:
                    break;
                case 3:
                    break;
            }
        }
        else//type is r
        {
            switch(refrence[refrenseIndex])
            {
                case 0 :
                    refrence[refrenseIndex] = 1;

                    break;
                case 1:
                    break;
                case 2:
                    refrence[refrenseIndex] = 3;

                    break;
                case 3:
                    break;
            }
        }
        System.out.println("modify bits is: "+ refrence[refrenseIndex] );
        pw.println("modify bits is: "+ refrence[refrenseIndex] );
    }

    private static int updateTLB(int logicalAddr, int physicalAddr)
    {
        for (int i=0;i<tlb_counter.length;i++)
        {
            if(tlb_counter[i] == 0)
            {
                tlb[i].logicalAddr = logicalAddr;
                tlb[i].physicalAddr = physicalAddr;
                System.out.println("put in ("+ logicalAddr+","+physicalAddr+")");
                pw.println("put in ("+ logicalAddr+","+physicalAddr+")");
                tlb_counter[i]++;
                return i;
            }
        }
        //tlb is full
        int freeTlbIndex = Algo.MFU(tlb_counter);
        tlb[freeTlbIndex].logicalAddr = logicalAddr;
        tlb[freeTlbIndex].physicalAddr = physicalAddr;
        System.out.println("replace in ("+ logicalAddr+","+physicalAddr+")");
        pw.println("replace in ("+ logicalAddr+","+physicalAddr+")");
        tlb_counter[freeTlbIndex] = 1;
        return freeTlbIndex;
    }
}
