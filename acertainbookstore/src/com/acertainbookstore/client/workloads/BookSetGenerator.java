package com.acertainbookstore.client.workloads;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import com.acertainbookstore.business.ImmutableStockBook;
import com.acertainbookstore.business.StockBook;
import com.acertainbookstore.interfaces.StockManager;
import com.acertainbookstore.utils.BookStoreException;
import com.sun.tools.javac.util.List;

/**
 * Helper class to generate stockbooks and isbns modelled similar to Random
 * class
 */
public class BookSetGenerator { 
	
	public BookSetGenerator() {
		
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
	 * This method returns a random number of StockBook objects from the store associated with the passed in StockManager
	 * 
	 * @param num
	 * @return
	 */
	public Set<StockBook> nextSetOfStockBooks(StockManager stockManager, int num) {
		
		Set<StockBook> randomStockBooks = 	new HashSet<StockBook>();
		ArrayList<StockBook> allStockBooks =  new ArrayList<StockBook>();
		
		try {
			allStockBooks = (ArrayList<StockBook>) stockManager.getBooks();
		} catch (BookStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		for (int i = 0; i < num; i++){
			
			randomStockBooks.add(allStockBooks.get(i));
			
		}
		
		return null;
	}

}
