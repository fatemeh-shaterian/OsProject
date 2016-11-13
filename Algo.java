package osproject2;

import java.util.ArrayList;

/**
 *
 * @author Fatemeh
 */
public class Algo {


   public  static int LRU(int[] tagIndex) {
        int min;
        int index;
        int i = 0;
        while (tagIndex[i] == -1)
            i++;
        min = tagIndex[i];
        index = i;
        i++;
        for(;i<tagIndex.length;i++)
        {
            if((tagIndex[i]!= -1) && (min>tagIndex[i]))
            {
                min = tagIndex[i];
                index = i;
            }
        }
        return index;
    }
   public static int secondChance(Integer [] refPointer,Integer []refrence,boolean[]refrenceFlag)
   {
       refPointer[0] = (refPointer[0]+1)%refrence.length;
       while (true)
       {
            for(int i = 0;i<refrence.length;i++)
               System.out.print("##"+ refrence[i]+"     ");
           System.out.println();


           for(;refPointer[0]<refrence.length;refPointer[0]++)
           {
               if(refrence[refPointer[0]] == 0)
                   return refPointer[0];
               else
               {
                   if(refrenceFlag[refrence[refPointer[0]]] == false)
                   {
                        refrenceFlag[refrence[refPointer[0]]] = true;
                        refrence[refPointer[0]] -= 1;
                   }

               }
           }
            for(int i = 0;i<refrenceFlag.length;i++)
               refrenceFlag[i] = false;
           refPointer[0] = 0;
       }

   }
   public static int MFU(int[] tlb_counter)
   {
       int max=0;
       int index = -1;
       for (int i = 0;i<tlb_counter.length;i++)
       {
           if(max<tlb_counter[i])
           {
               index = i;
               max = tlb_counter[i];
           }
       }
       return index;
   }

    public static int OPT(ArrayList<op> operations,int nowPointer, PhysicalMem[] physicalMem,PT_S[] pt)
   {
        int ffIndex = -1;
        int[] tagIndex = new int[physicalMem.length];
        for(int i = 0;i<tagIndex.length;i++)
            tagIndex[i] = -1;
        int counter = 0;

        for(int i = operations.size()-1 ; (i>=nowPointer) ;i--)
        {
            int temp = findInPhsycalMem(physicalMem,operations.get(i),pt);
            if(temp != -1)
            {
                tagIndex[temp] = counter;
                counter++;
            }
        }
        ffIndex = getMin(tagIndex);
        return ffIndex;
   }
   private static int findInPhsycalMem(PhysicalMem[] physicalMem, op get,PT_S[] pt)
    {
//        for(int i = 0;i<physicalMem.length;i++)
//        {
//            if(physicalMem[i] == get.page )
//            {
//                return true;
//            }
//        }
//        return false;
       if(pt[get.page].valid)
           return pt[get.page].physicalAddr;
       return -1;
    }

    private static int getMin(int[] tagIndex) {

        int min;
        int index;
        int i = 0;
        min = tagIndex[i];
        index = i;
        i++;
        for(;i<tagIndex.length;i++)
        {
            if((min>tagIndex[i]))
            {
                min = tagIndex[i];
                index = i;
            }
        }
        return index;
    }

    /*
     *
    public static int LRU(int[]tagIndex,int nowPointer, int[] physicalMem)
    {
        int ffIndex = -1;
        int[] tagIndex = new int[physicalMem.length];
        int counter = 0;
        for(int i = 0; (i<nowPointer) ;i++)
        {
            if(findInPhsycalMem(physicalMem,operations.get(i)))
            {
                tagIndex[i] = counter;
                counter++;
            }
            else
            {
                System.out.println("##"+ i);
                tagIndex[i] = -1;
            }
        }
        ffIndex = getMin(tagIndex);
        return ffIndex;
}

    private static boolean findInPhsycalMem(int[] physicalMem, op get)
    {
        for(int i = 0;i<physicalMem.length;i++)
        {
            if(physicalMem[i] == get.page )
            {
                return true;
            }
        }
        return false;
    }

    private static int getMin(int[] tagIndex) {
        int min;
        int index;
        int i = 0;
        while (tagIndex[i] == -1)
            i++;
        min = tagIndex[i];
        index = i;
        i++;
        for(;i<tagIndex.length;i++)
        {
            if((tagIndex[i]!= -1) && (min>tagIndex[i]))
            {
                min = tagIndex[i];
                index = i;
            }
        }
        return index;
    }
     * */

}
