package com.wistron.pqaa_common.jar.algorithm;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class WisPi {
	/*
	 * Machin algorithm:  π=16arccot5 - 4arccot239
	 * arccot(x)=1/x-1/(3*x^3)+1/(5*x^5)-1/(7*x^7)+......
	 * 
	*/
	private static final BigDecimal TWO = new BigDecimal("2");
	private static final BigDecimal FOUR = new BigDecimal("4");
	private static final BigDecimal FIVE = new BigDecimal("5");
	private static final BigDecimal TWO_THIRTY_NINE = new BigDecimal("239");
	
	/**
	 * Get the calculated PI value
	 * @param digit
	 * The decimal digits
	 * @return
	 * Return the PI value
	 */
	public String getPiValue(int digit){
		int calcDigits = digit + 10;
		BigDecimal pi=FOUR.multiply((FOUR.multiply(arccot(FIVE, calcDigits)))
			      .subtract(arccot(TWO_THIRTY_NINE, calcDigits)))
			      .setScale(digit, RoundingMode.DOWN);
		return pi.toString();
	}

	private BigDecimal arccot(BigDecimal x, int numDigits) {
		BigDecimal unity = BigDecimal.ONE.setScale(numDigits, RoundingMode.DOWN);
		BigDecimal sum = unity.divide(x, RoundingMode.DOWN);
		BigDecimal xpower = new BigDecimal(sum.toString());
		BigDecimal term = null;

		boolean add = false;

		for (BigDecimal n = new BigDecimal("3"); term == null
				|| term.compareTo(BigDecimal.ZERO) != 0; n = n.add(TWO)) {

			xpower = xpower.divide(x.pow(2), RoundingMode.DOWN);
			term = xpower.divide(n, RoundingMode.DOWN);
			sum = add ? sum.add(term) : sum.subtract(term);
			add = !add;
		}
		return sum;
	}
}
