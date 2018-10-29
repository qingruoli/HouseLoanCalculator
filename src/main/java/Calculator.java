import java.math.BigDecimal;
import java.util.Map;

/**
 * 
 */

/**
 * @author caoqifa
 *
 */
public class Calculator {

    public Map<String, Double> calculateAverageCapital(double loanAmt, double YearRatePresent, double monthRatePresent, int year, int rateType){
        BigDecimal rate = BigDecimal.ZERO;
        if(rateType == 0){
            rate = new BigDecimal(monthRatePresent/ 1200).setScale(5, BigDecimal.ROUND_HALF_UP);
        }
        int period = year * 12;
        BigDecimal repayment = new BigDecimal(loanAmt).divide(new BigDecimal(period), 2, BigDecimal.ROUND_HALF_UP);
        BigDecimal balance = new BigDecimal(loanAmt);

        BigDecimal loanInterest = new BigDecimal(0); // 当月贷款利息
        BigDecimal sumLoanInterest = new BigDecimal(loanAmt); // 贷款总
        for(int i = 0; i < period; i ++){
            balance  = balance.subtract(repayment);
        }
        
        return null;
    }
}
