package com.acertainbookstore.client.workloads;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import com.acertainbookstore.business.StockBook;

/**
 * Helper class to generate stockbooks and isbns modelled similar to Random
 * class
 */
public class BookSetGenerator { 

	public BookSetGenerator() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * Returns num randomly selected isbns from the input set
	 * 
	 * @param num
	 * @return
	 */

	public Set<Integer> sampleFromSetOfISBNs(Set<Integer> isbns, int num) { 
					
		Set<Integer> randomIsbns = new HashSet<Integer>();	
		
		int i = 0;
		
		/*  Set are not ordered so the first num will be random.
		 *  This for loop stops adding to the return set once i == the number desired. 
		 * 
		 */
			
		for (Integer isbn: isbns){ 
			
			if (i == num){
				
				break;
			}
					
			randomIsbns.add(isbn);

			i++;
		}
		
		return randomIsbns;
	}

	/**
	 * Return num stock books. For now return an ImmutableStockBook
	 * 
	 * @param num
	 * @return
	 */
	public Set<StockBook> nextSetOfStockBooks(int num) {
		// I do not understand what this is asking of us...
		
		return null;
	}

}
