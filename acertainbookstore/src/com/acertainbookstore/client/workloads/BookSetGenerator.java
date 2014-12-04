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
		
		Random randomNumber = new Random();
		
		for (int i = 0; i < num; i++){
			
			int random = randomNumber.nextInt(isbns.size());
			int j = 0;
			
			for (Integer isbn: isbns){
				
				if (j == random){
					
					randomIsbns.add(isbn);
					break;
				}
				
				else {
					
					j++;
				}
			}
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
		return null;
	}

}
