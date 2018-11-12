import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * 
 */

/**
 * @author caoqifa
 *
 */
public class ParameterRequest {

    public static String createParam(int com_amount, int year, double fund_amount, double com_rate_percent, double fund_rate_percent,
            String pay_method) {
        String str = "http://fangd.sinaapp.com/home/calc?com_amount=" + com_amount + "&fund_amount=" + fund_amount + "&year=" + year
                + "&com_rate_percent=" + com_rate_percent + "&fund_rate_percent=" + fund_rate_percent + "&pay_method=" + pay_method;
        return str;
    }

    private void writeSameAll(){
        String pay_method =  "same_all" ;
        try {
            File file = new File("same_all.csv");
            // if file doesnt exists, then create it
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
            System.err.println(file.getAbsolutePath());
            FileWriter fileWritter = new FileWriter(file.getName(), true);
            fileWritter.write("\"贷款总额\",\"贷款年数\",\"贷款月供\",\"贷款年供\",\"每年比贷1年少还\",\"贷款利息\",\"前10年还贷\",\"10-15年还贷\",\"15-20年还贷\",\"20-25年还贷\",\"贷款总额\"\r\n");
            double lastYearPerMonth = 0.0;
            for (int year1 = 11; year1 <= 25; year1++) {
                int com_amount = 35;
                double fund_amount = 0;
                double com_rate_percent = 4.90;
                double fund_rate_percent = 4.00;
                String url = createParam(com_amount, year1, fund_amount, com_rate_percent, fund_rate_percent, pay_method);
                String jsonStr = HttpClientUtils.sendGetReq(url, null);
                JSONObject json = JSON.parseObject(jsonStr);
                double sum_com_interest = json.getDouble("sum_com_interest");
                double sum_com_all = json.getDouble("sum_com_all");
                double sum_fund_interest = json.getDouble("sum_fund_interest");
                double sum_fund_all = json.getDouble("sum_fund_all");
                double sum_all = json.getDouble("sum_all");
                double sum_interest_all = json.getDouble("sum_interest_all");
                double sum_base_all = json.getDouble("sum_base_all");
                double sum_com_base = json.getDouble("sum_com_base");
                JSONArray detailArr = json.getJSONArray("detail");
                JSONObject detail = (JSONObject) detailArr.get(0);
                double per_all = detail.getDouble("per_all");
                double deposit =lastYearPerMonth == 0 ? 0 : (lastYearPerMonth - per_all) * 12;
                lastYearPerMonth = per_all;
                double firstTen = per_all * 10 * 12;
                double yearAmt = per_all * 12;
                double ten_fifteen = year1 > 15 ? per_all * 5 * 12 : per_all * (year1 - 10) * 12;
                double fifteen_twenty = year1 > 15 ? per_all * (year1 - 15) * 12 : 0;
                double fifteen_twenty_five = year1 > 20 ? per_all * (year1 - 20) * 12 : 0;
                List<Object> list = new ArrayList<>();
                String content = "\"" + com_amount + "万\",\"" + year1 + "\",\"" + per_all + "\",\"" + yearAmt + "\",\"" + deposit + "\",\""
                        + sum_com_interest + "\",\"" + firstTen + "\",\"" + ten_fifteen + "\",\"" + fifteen_twenty + "\",\"" + fifteen_twenty_five
                        + "\",\"" + sum_com_all + "\"";
                System.out.println("Done");
                fileWritter.write(content + "\r\n");
            }
            fileWritter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void writeSameBase(){
        String pay_method =  "same_base";
        File file = new File("same_base.csv");
        if (file.exists()) {
            file.delete();
        }
        try {
            file.createNewFile();
            System.err.println(file.getAbsolutePath());
            FileWriter fileWritter = new FileWriter(file.getName(), true);
            fileWritter.write("\"贷款总额\",\"贷款年数\",\"\",\"贷款年供\",\"每年比贷1年少还\",\"贷款利息\",\"前10年还贷\",\"10-15年还贷\",\"15-20年还贷\",\"20-25年还贷\",\"贷款总额\"\r\n");
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) throws IOException {
        ParameterRequest req = new ParameterRequest();
        req.writeSameAll();

    }
}
