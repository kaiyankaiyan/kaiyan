package com.haoyong.sales.custom;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;

import net.sf.mily.ui.Hyperlink;
import net.sf.mily.ui.TextField;
import net.sf.mily.util.ReflectHelper;
import net.sf.mily.webObject.Field;

import org.junit.Assert;

import com.haoyong.sales.base.form.BOMForm;
import com.haoyong.sales.base.logic.SupplyTypeLogic;
import com.haoyong.sales.sale.domain.BomDetail;
import com.haoyong.sales.sale.domain.OrderDetail;
import com.haoyong.sales.sale.form.OrderTicketForm;
import com.haoyong.sales.sale.form.PPurchaseTicketForm;
import com.haoyong.sales.test.base.AbstractTest;
import com.haoyong.sales.test.base.ClientTest;
import com.haoyong.sales.test.base.CommodityTest;
import com.haoyong.sales.test.sale.ArrangeTicketTest;
import com.haoyong.sales.test.sale.OrderTicketTest;
import com.haoyong.sales.test.sale.PPurchaseTicketTest;
import com.haoyong.sales.test.sale.PurchaseTicketTest;
import com.haoyong.sales.test.sale.ReceiptTicketTest;
import com.haoyong.sales.test.sale.SendTicketTest;
import com.haoyong.sales.test.sale.StoreTicketTest;
import com.haoyong.sales.util.SSaleUtil;

public class SpecialOrderTest extends AbstractTest {
	
	private void set服装生产() {
		if ("订单单头".length()>0) {
			List<String> trunkList = Arrays.asList(new String[]{"number","proType","orderDate","hopeDate","saleMan",});
			List<String> requires = Arrays.asList(new String[]{"number","proType","orderDate","hopeDate",});
			LinkedHashMap<String, String> renames = new LinkedHashMap<String, String>();
			renames.put("hopeDate", "订单交期");
			LinkedHashMap<String, String> inputs = new LinkedHashMap<String, String>();
			inputs.put("proType", "SelectListBuilder");
			LinkedHashMap<String, List<String>> selects = new LinkedHashMap<String, List<String>>();
			selects.put("proType", Arrays.asList(new String[]{"打样", "大货"}));
			new OrderTicketTest().setSellerViewTicket(trunkList, requires, renames, inputs, selects);
		}
		if ("订单明细".length()>0) {
			List<String> trunkList = Arrays.asList(new String[]{"spnote", "XS","S","M","L","XL","XXL"});
			new OrderTicketTest().setSellerViewDetail(trunkList, null, null);
		}
		if ("商品".length()>0) {
			List<String> trunkList = Arrays.asList(new String[]{"name","color","supplyType",});
			List<String> chooseList = Arrays.asList(new String[]{"picture",});
			List<String> titleList = Arrays.asList(new String[]{"name",});
			List<String> requireList = Arrays.asList(new String[]{"name","color","supplyType",});
			LinkedHashMap<String, String> renames = new LinkedHashMap<String, String>();
			renames.put("name", "款号");
			new CommodityTest().setSellerViewTrunk(trunkList, chooseList, titleList, requireList, renames);
		}
		if ("物料".length()>0) {
			List<String> trunkList = Arrays.asList(new String[]{"name","color","supplyType",});
			List<String> titleList = Arrays.asList(new String[]{"name",});
			List<String> requireList = Arrays.asList(new String[]{"name","supplyType",});
			new CommodityTest().setSellerMaterialTrunk(trunkList, null, titleList, requireList, null);
		}
	}
	
	private String get订单() {
		OrderTicketTest test = new ClientTest().getModeList().getSelfOrderTest();
		OrderTicketForm form = test.getForm();
		ReflectHelper.copyProperties(this, test);
		this.loadView("Create");
		this.onButton("生成单号");
		String number = form.getDomain().getOrderTicket().getNumber();
		this.setFieldText("proType", "大货");
		this.setFieldText("saleMan", "业务员001");
		this.setFieldText("client.name", "轻工");
		this.setFieldText("orderDate", "2023-11-20");
		this.setFieldText("hopeDate", "2023-12-15");
		List<OrderDetail> detailList = form.getDetailList();
		if ("第一行明细".length()>0) {
			this.onButton("添加明细");
			OrderDetail detail = detailList.get(0);
			this.setRowFieldText(detail, "spnote", "<img src=\"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAG8AAABNCAYAAACooTpdAAAUL0lEQVR4Xu3dA5BsybYG4Dxj27Zt27Zt27Zt254Y27Zt28a798v38kTdeoXcu3Z310TcFVFxeqY2MnPpX8isfv/zbwr/pX/kCvT7L/P+kXyLg+4R5n388cfhmmuuCSOOOGIYaaSRwu+//x7++OOP0K9fv/D3338Hyj7AAAOEQQYZJPz555/hr7/++t/B/Pv7gQceOP7tGb7bcMMN43V9Se+880749ddf4ziMe8ABB4z/movPoIMO2n9+5mDc6Ttz++2338IQQwwRJppookqnUTnzTHLvvfeOjLj//vvDKKOMEuacc84w+eSTh8EGGywO3oQuvvjiMP3004dxxx03DD/88PH/ffbZZ+H5558Pjz/+eBh66KHDBBNMEKaaaqqwySabVDrpIg+7/vrrw1133RXGH3/8yBSC52N+aS7PPPNMmG666aLgYZrv0veJ2S+99FLYcsst43VVUWXM++STT8INN9wQrr766rDbbruF+eefP07k3nvvjQwx8dFHHz0uwqijjhqOOOKIKMGTTTZZZOwbb7wRvv/++8js2WabLUwxxRRxjvvvv39clHXWWSeMNdZYVc076zmsh7Hvt99+La/fY489wmijjRYWXnjhOJdG9MUXX4SDDjooHHzwwWGooYbKen+7izpm3qOPPhruvvvu8MMPP0QtufPOO8POO+/cf/HTAD799NPw2muvhQ8++CB899134ZdffonMG3LIIfszddJJJ+1vNt337bffRiaTXgz073zzzRfmnnvudvMq/b15PPDAA+Gpp54KTz75ZJh11lmjgCUXYOGZQCbUmAjl0UcfHTUK4wgoN/Hzzz+HH3/8MXz99dcB43zuu+++6EZmnHHGsMACC4Sxxx679DjdWIp5bPjNN98cHnvssWjeDGT22WePA+GrDj/88LDggguGJZZYIgw00ECFB0hjb7/99nDjjTeGpZdeOiy66KLxGcwpTf7qq6/CNNNME5Zccskw3HDDFX5+oxuYPmb+ww8/DOONN15YaKGFwsQTTxxeffXV8OabbwaW5ZtvvolCl8ynuXETxrriiitGhmIck+k7/43ZI488chhzzDHDtNNOG3766adwxx13hKeffjqu3VxzzRXdSvL1RSZTiHm05tZbb40aZGKY00h6DHD55ZeP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\" alt=\"\">");
			this.setRowFieldText(detail, "commodity.name", "S16VKNI00247");
			this.setRowFieldText(detail, "commodity.color", "白色");
			this.setRowFieldText(detail, "commodity.supplyType", "生产");
			this.setRowFieldText(detail, "XS", "56");
			this.setRowFieldText(detail, "S", "101");
			this.setRowFieldText(detail, "M", "179");
			this.setRowFieldText(detail, "L", "157");
			this.setRowFieldText(detail, "XL", "67");
			this.setRowFieldText(detail, "amount", "560");
			this.setRowFieldText(detail, "cprice", "100");
			this.setRowFieldText(detail, "cmoney", "56000");
		}
		if ("第二行明细".length()>0) {
			this.onButton("添加明细");
			OrderDetail detail = detailList.get(1);
			this.setRowFieldText(detail, "spnote", "<img src=\"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAGsAAACcCAYAAAB4KFWtAAAgAElEQVR4Xu3dA7AkS9YH8Oq1bdt+a9u2bdu27X3r3be2bdu2baO//mV8pyNvTSmr6r7p3piM6Lgz3VWVOPofZNZiuWrVvrYVK7DYR6ytoFMa5D5ibQ+t9hFri2i1j1j7iLVNK7BFY91ns/YRa4tWYIuGutGSxQVcLBZbtJzNQ51rHhtLrH/84x/VQQ5ykOrgBz/4rhDLAv7pT3+qDn/4wx8oDPG3v/2t+vznP1/98pe/rM53vvNVRznKUYrnNYhYP/nJT6rjHOc4aVL//e9/0985ON6C+SBKtD/+8Y/V+9///uo3v/lNdclLXjIt5uEOd7jiiTXdkHP4f/7zn+phD3tYdepTn7q66EUvWh35yEfeccsYaajfow9E+uxnP1t9/etfr/zffM50pjNVpzvd6db9De2rl1i44eUvf3nq5ApXuEJ1ilOcojrEIQ4xy+LFQ/76179W3/rWt9Lnq1/9anX605++OvShD52IdrzjHa+60Y1uVB32sIedpc+///3v1Wc+85nqrW99a3XGM56x+sMf/lBhxvOc5zzVKU95yuoIRzhCdchDHjL1P6Z5/o9//OM0l9///vdpPtburGc9a2LKpz3tadVvf/vb6upXv3p14QtfuDr5yU8+uJsdxMoprMM3velN1Y9+9KMktp/61KeSFJzqVKdKXEjS/P8EJzhBmlgJAUknTkOkL33pS9Wvf/3r9Ewqz/NJ1ZOe9KS0aNe73vWqj3/844kTSdqxj33swZPLL9TPS17yksQAv/jFL1JfiHWhC12oOvrRj14d8YhHTBJgXsZz0pOeNDHIcY973KSyzNFik/zDHOYw6d8HO9jBkrT8+c9/TkTHBD//+c+rf/7zn2k+CPGDH/wgMYD5vvnNb073m4v//+Uvf0nzvdjFLlZd85rX7J3bHsSix+94xztW733ve9MA73GPe1RXutKVqpvf/OZp0De+8Y2rL3/5y4k4P/vZz9J6GNyxjnWs6khHOlIaGBVpIv/6178SVyGOSSHur371q+rf//53YgLfueec5zxn4m7/f+c731m9+tWvTs9+9KMfXd3ylresbn/72yeCUSmXuMQlqsc97nFFqlG/V7nKVdIC3exmN6sueMELpoV9znOeU33nO99JHH61q10tLbB5+I5twTT+TxKMGyHNy5wQx/dHO9rR0py/+93vJnXqO79bu6985SvVK1/5ykR8a4GxaY1rXOMa1QlPeMJkw/z+mte8prrd7W5X3fve9+5kxD3U4De+8Y1EGBxtAhbOYlus5z3veWlhEY+uxyE//OEP02AMmNh/+MMfTgMKornXRHGcey04YpLWr33ta0llIJ4Bu9ckT3va0ybuv/a1r504HvdThy960Yuq173uddUzn/nMxPlDmzFguutc5zrJXmiY45Of/GT17ne/O0mUcWGcy1zmMmlupP7MZz5z+v6jH/1oIhSpJpHGdvzjHz+pOr9Tnz/96U8roOh3v/td9fa3vz0xm3+TQOt0/vOfvzrJSU5SXeACF6gOdahDrYeOQc3JOt75zncuI5YBWJS73OUuafAvfelLq0984hPV2c9+9uqNb3xj9b3vfa/65je/mYiBk1xzspOdLC3Cpz/96aQiznGOc6RJfPvb306Ts/gWhWTiTCoJZ9LnYXgtBm4zMWoFAc9ylrMkRsA8uN7kqceb3OQmabFK2gMf+MDELJgk7B/7YpGNxZi++MUvVh/72MdSn+aBaKTS78GApM0a+T/pw3DmSCNRcZo5nu1sZ0vrQJp8PKvJVOj/Xe96V5K8m970pmXEwnFPfOITk2Sd4QxnSIOhEnEEzseFoQJJIQ4yIZPTIYIc85jHTKrRZHzP9pAgv+NyDbFN4qhHPWrS26QNgY5xjGOk31x3mtOcJknr5S53ucS1CEtt3O1ud9sDvfUR7sEPfnB1rnOdK3G48eT22SIjiDFbPEyE0Yz3+9//fiKe/v0eaPigBz1o+h0TYVaSj9kwAgZmr3yXS1ET6tMv7YXAN7jBDcqIZYGf8IQnJFuhM4MiXYhg4MSayIaIM5IIYqFNyv9xmsn5S8yDiIG0wo65xwQ824Qt5olPfOKky3Gqifg/yfQ8dm7//fev7n//+6c+S9pjHvOYJI2XutSlkmqtt4DZCGJcpM5f3wMJ1LO5mQtmjXH7nt2yTj6IFRI0BJJjYibAWrOrXW0Pm4UIj3rUo6qrXvWqSbJ0/JGPfCQZWmrChMFQ6pBk+YtIJkd9xWRwoMEbcEgT6cHViEX/IxCp8uxoFghq8j3VCSVZDMT7whe+kOzBAx7wgGJnGSjB+Yg11A0wdsQyD3+NP3zCOfxMc6ZWX/jCF6b5XvrSly4jlkV56EMfmuwCg0g8EcTCGTSOppbqE7bIVBbDjANxKLul4T6OLcmiIrqiElTds5/97OTPUad8OwvkWX6jMoyvdLGe+tSnpnHgXmPYlEZbveIVr6j222+/hAGKJIuEPOQhD0moCBoyQRxG2qgpC0cPM6C70V772tcmSUJ4oIMUBweyl+zmgx70oOKun/WsZyUtwRkdKlnFnYy4AVKm2s973vMm8FNELBJxv/vdLxk78DiMMY//RCc6UYLwpISvMHdjo1784hcnaQJ0gI/w8EksNfy2t71tFLGe+9znJukMtTr32Mc+j39mztQzxF1ELLqZTbj4xS+e4DjVpfFJECp8D44kFTlnE13gSLKLpOiud73r2vml20kVuwVglLbnP//5ya5CuXPFGkvH0HQ9QPf0pz89mRaqsIhYwMB97nOf5JACABEjw/UveMELEuiA1kQSQOs5Gx+HfQQE2E59hG0iWfw9YOe+971vcbcHHHBAciUQi8RuQmNe2CyMdOUrX3ntsLeNbQ806AEki+SQooC5JE7kQKyMnkVIkYU5GzVnQcUhr3WtayW1Gw3ifN/73pfCOqIRpe1lL3tZQl7UOzS6Cc1ai+wAP+abR+KbxtcYdadmUBoiy/0ZEQrwHMwUw9PBXPkmkgTtAS/guUg7oBGNZH3gAx9I6pHkl7ZXvepVyV9CLIHbTWgEgGTRWNayT1M1Eou3z9Nns3KYKybm4WyKBSVZnNY5mnCPaAk1i2hXvOIVdzyWNIukWPC73/3uxV2C/Jx5LkmTU1z8wBluYHI4xZDq9a9//RRjLLJZLiZZ/BFILJcsYgtac4YtKmgtfje18dFe//rXJ0JgDuq3/ly+23ve855EyLHEYg9lDTbFZiGWMQmQ0yRMSxGxEISagQble+oZ1A996ENpwUBrsP5e97rXVFol1SocBLrq318xwryRLKpXLBJKLG3UYEjWpjjFiCWERrIQqy8RuYca9AB+Fh0qglGPwZnwM57xjPQb+8KZm6oKBU2pQe4BidV3vbFZJAu0v+c971lKqxT5AE4syqbYLGttTNAglAoLFEmW2J4IASeNwatLFqdZqoRNe8Mb3pAiGZEjKl7B/79BvgiYkPQT1JVaqDdIjmRRlWOIpTRBRJ0a3BTJokVoKCgbgwpYFxELBwMYJiUS3BTd5piKgkMzssaMdl70UkI0KlBsTChL9lbqvonzjQt013dfRrWpf5kDqpRkbQrAQCxqMIglR1ZELBD6EY94REJjoHsTF+LQd7zjHUnyRAX4Xn3GsW0Q1AA1SCXwfyL+2CRZknSkbwx0F9mWUbjhDW+4MZJljoEGZd/7wNoeNksAVdD2Fre4RUpd5MmzWEAxtre85S3JblFhwiQWeUwTYkIwyU3ARQC5qelThOODH/xgJetb2gRLIUqSFSG00mfsxvWhBoWbimODIghPecpTUj6LtLRFqIV+qC8dIFqUWpVMCAE8g7TIb1EDbZFnUiHUxDEfQyygiPRe97rX3Rg1aK2oQQCDz1ocdWfA5ZNuc5vbJKPfVmJmgcF4vpaaBNHsUv/FM+TJIEAON8J7XlOjbtkroObhD394CU+kax//+McnIjHk7DB70dbEI+P3/Lq2HNqQjHBTX57HjoLu5s5dKrJZHF5xNCkQkLxJDXogqYCweN0W0rWlEJ4KZf+ErCQ2qcC2BQFEqEFRDMQqTT6ywyIvnH3BaeBIayJa/uyoQnZt/n3cF4TNCZx/V1/8qOHwvawFTcYpBtvrUZv6vXvYLEFU4OFWt7pV4sSuNAgIj5gcVdf2dZZ3brFUp4L9kKWm2LGtcRlIsDiasueSolLPFKknUWxjpOkhWB/uSp6uRwgfTGTho+bRv91rwX1cE6ULcU3ESqOUIcobjCFKA4KY7gXomAKB8yb/Ml+PPYgFHiMYdMIWeWBb4yCDnQy2iIMgadf1+XOIP3+ONOJ08F9EpItYUjMmJitQChIAJgRRURULnhMo5/iQItLsWgzpWotMiwRzYRgf1/gNODMH3wWx4lm55MUcfYcJBae5LApQi9SggkOSQg1yiLv8J6rQwpEOkiU8xTcb0kQiMIU8Gb9K/qyr4UrGGPhRDMldGNrcK0RF8tUOmlPYmbq9scghcUAUkCWNIUWj2tZY2WYEIxUAGHBEaiE7TGRsOfFzlWnMeZ/mr0CI069Ws4hYUswGQixJVl8DvU2GPyYDK/zU1yIg7DpSRboUQXY197CnIDj43eft58+yqEJoGELEZai9gzwxHz+QFnEfNWqBqTsE5KwHKIoa+KEMa4yIRJsJLvSh3D3UIM7VuUHl+aQuVSjdTwqpAZC/bzFcZyEQmDoUuuorYkEsE1PTCDUZ39BGIgEMnDu07JrWYCOpNBIFrWJG/iTmjBI7PiJwgGikilob2ofx64dLIpSmtr9IsiAtUFrWMs/Utj0kwkB8JOkTaKuvtJkKlLvidMuPqUHvI7D+pRP4JFIobOrQ9rnPfS7dR30OmZPnIpDoApWIAKSIbRK3pOIQDCGVktMKqnoxnGtFfoY26xe2WOahCzjtIVmCpMq1GOIhkmVQxJjepTrpc3ahreFIFajEXtCWlPRFm+NZJJKaZvD7jHHev8osWWbpnKFBXPaHJEfpHRcDuOC4Ut2iLcJj/FI2ChgxLsTqU+n52OTySKeSdTHPLhW6g1g6FMRFLEUrQ1MJuEt6Q4ELtCbr2YbWLADH1rWxcS6vyO3iSD6Jez2jJPJua09scBjquJN4tok0WUDagBSI33kGzUD1WzPuDSJFBW9fej6fY5Rnc2NA96iTbFqHHcRiSBnw2972tmvAMESccbxFhJzoX1xfT63Ec/yOuDhUDksqps3xrvcNKLB14pKPfexjhwwtXQOxWnChpqE7GhFLf4hl8SFXalikgSoFdiBgkoeIAetJSgn4MT6MxBZjhK569x3EYuSkKYAEarDL76mvFGIZNH+Lk8lvqDff4yCTha5Mtm/nRP4MgVg+iQjLIx/5yEG+Fvtio5roCAYZWtlE7bFDiCBKQ20jFhVP42Bs6C98JbZG/JKk9KU66usCvLD3mKOrZGEHsRQb4nITM6CSHBW7pZQMkU3q1re+9R4SYzAytohF/0NNJcYYsRl2hLJni1/X1/iMVCY/C8cPnZMFtPgkS7odkxhv5MLYGWtFkjwTY1Ob0F0psdzHpsqE23/W1nYQi4HjJxlcXz1A/YEGL25nQfwbHK+jQnqeejEhiyhYXNoEf5VCU03UWl8DLlRi4dgS/4dq51aQFAAIoqTmQzJpBoTEdFAgwtEsmKkvL1UfM6awZrQORqzXn8T1a2KhrgnhQuBgqCHOOxZTtOudKgV96/BaRAAxcZHFANlLG9UEeYK7fX6JZ4sjUlVinW12tGkMpBix2BOuDIDBeY/MOTXpucYD1fLBEJYtE9EZGnbTN1WNidnhy172sq1bf9bEosbYLNEBTvGYenALSGqgNjoclA+7x97gHN+Bu4g2FLLni2nxcLmttHJUXeMUiTEfJQpQ1lDYHv1R2zieugXVqfggFgnSMKX1imdjVD5qib33HK4CLaAfe8ma2ppYEBNbAhgQwxLOiAeDsbYL4UQLxfbZY6VBcZH/8n8bx4Ya+3zgnotgxqtq2HPa8kl2nCCqUFMJnI7+qDXIlaSQGGo9iBUq0lhIXERgMBImHIo6oy9EB2KgQvnEJrcpEYsoM9j26uLUvsrQNtUVO89JFpUn0n3uc587XW7R2Bv6nH6XDhlq7KO/IAokxj7qg/1qIxY16xgFW4ja7ECXGuY7IRLAQMIwXhCFncE4fD4gKVIjEqSASKlm8iwMQLVjCrSot0QsKNBgQFyTKuWK/KEgKP1LqgR5EYzEQVMmj2hySyUoMCeWf2MuiJN99SygqB5Ft1HcISUhVX2xxyaieSZuF5EAiNimeI45+ZCIXGr5jha7VOXqHzP70BrGXo8xLlaDWPKr4iCQUoeuPklBU5IF0uoY0YAX0iCVwK5xhMdMJvoiwZ4t00yyAI68vExg2QZ23Km2Qb9DYo9NBINg2SqcTyOEGjQ/hPLJbS8gwoaVgJnol70HXITUMLpoUJgR1yxWIY4lg0j3U39TpMoDoSh1FbiLmmK/GGPPFZZhSMegwPpCYgAEk/wEXkhs2FkuASmQEjGOUpWU90VLYATjR/AcOFCBpEtoLhqAQDuNOfXMM2gMGki5HWSodC7aYjWhpeN/TAoVx3JgPkGoEjSXToid/jiSPud/jbWJeR/gLsKTZAYZMgxG41iSpstf/vKDQ1lttgunYwJrE4nJuBYTsru5PQRIuD1jXJ94LtCizpEQiaGuibUK3C4d+zMGRrdNEMdTH9ShSADH0cJRJVRKiXPa1kd8z0YIdeW7IakPaooEj608iuezjyB8U2YasTBhHrQOSRyjBvUZ42W3aIacsRerMqglAzkkw9u3cPG7hcLt1BO/inQZBDgqUFnqg3T1S7cDL/m5R/pTDDrHDhdqncolKXWtw14BHPl8IuY5pUQbUwNGGDBnhMWqQmlpMENCN0OJ5Tq1HJJ3JgjW4kDSO/eRDNSFxVK3GC12usgOz9FohiiXi+dhPoSMCqj4nmrmP5YW9OTjtG4Qer0+crECAUtcOGbPU9dCABkQHyhP7dH54o0Ax5xNLI1WyDWDxaVGcOcUcJETxr/b6gbz+fCV4pCWsfMEzGiM+t7pxYoLlypCndoyZ7OJgEPpIwdkAhzktorbsX1TFU4Tq6fr7d6MszzGPnvMfRAiLTIFVcuks5M5EkzMsoKeS9lhNXxDk4BDJsHo84MADc8lUVTV2N0mTX3G9iRSVF8cQVFMAn0emA0gMd/SItR8jHbvW6d6TjBFMBhivsmcKE1mFTKifw2c36EiaU7JYpuk7MUj643fxZ70VbnOTcjIb5WG0vJxEBwhsvomxUQshswhT31bTkomJvqMWOAtAACCqkMsKc7s60/tO93ehPrksPg8jmzdpga4sFV3uMMd9lirRCwwG8fzmOdqJAvAUA8vhMXRY7OGFI4OHQPwEoUw9XvAeQcyU5Hb1CBzmkIeru7iJGKZtIiDvM9cDbGkRdiVOPOWz9VXU1jSP2CkMorKqDeRB9t8bAwck+4pGcec1wooqHFsAnyJWA6wkvgas++pbaCeCQmKnQluIhQfq6RatW8RwHYpkKY6RbbDZgeSNac0941p6u9Ambxf05FHiVhsi5Q+ozwXImQzhJbknXjz8lgiGnPaRbCdNmgjBuhrM0LJVqSpiz31fmaDVpA1qLdELKpKGEhV6BxBVp3ws8BY5QKIxqFU9tx3WuXQyYpMI1Y9JJPfz9EXJpoj7DR0XFOvs7EOczfVD67T+lLkKmnn4kInugjHqLqNw01EwudiBpECOTghpbZMgd+lLMD7bWn8Q/X0TfuL18RSWCKg25ROHjNRksUL5xzLtOIW6fG5AsbsobMtOPRtTfRd7NC5UFOc1DHzH3uPyIssSFPt4ZpYkl2iyPJCczRIUFxOqTMIT7qow74d6UP75mMpfeva0yTmKfVPyuf074aOccx1/CvlFU2RnjWxRHmBAhWzc3Ch9L2qHzqYfxV7d6OAZsxE8nv4UHypLj8KCgVAnvzkJzceMTR1DHPfL7kpzklbNEWT1sQSPDQpdeRzhISoQdEFMTtAg4qlDpvOZRozab6IIpauo4FkkSUg73SnOxXt5xoznjnusU5QoA2NTZnmNbFAbPpSIeaQGvK+weF8EWgSRg1K8YsPzpXP4vBqXS9biapfAVGqZdObOCrGciJBUzXWmlgiDi4EhZt2gJROlL8g4q7uAriADPk9cwEMjMUOKXVra1wSv7uOA73pjdo2L2V0TVGXNbEEPe3LspGu720zQyZNoiwStRoFJFFSPeT+vmsEaDnYXRluDELy/I2Xp/U9d2/+roRORS4U29TWxFJ/bmIiDWNOHas/HBrEHUIniAZgiOONPZCr/nzAQUywKS4Y14pgm4tUCp8rr8Hbm0Rp65udB8gweCexpDIYa1KghHdqSRrIDlzYvsoZpgrZrLmSgYADDdB3XDmn2b4xUYy5SwrmJriAunVTuNpJLGAAZBQAFdCdUp2jI5IVteJsh8Qjhpgr3ORQFUcl9MUahdDk1hB3Dls8N4Hy54nNYm6Z4k5igY24UH6IIzk1Og62K2uWk0EkaRKxwT5JGLIYyqcRS0a1r9wbDFavbt+YcNomN3lFKRJpnU5i4X7nMCjMZLSn2haugPidIk9RZKkMEYwxW2/qA1cTbozG25f+wKWyCuzl3BVccxPeWCUf2zDDGmAoLBREjLJngd0pzd4oEiCQytDzyNnDvjP1hvQp5ihXFWXSXffEAiDw3BVcQ8Zacg3fEWKGyjsli60iflCb0t+mfEpJx5CNjQgkC3SOVwFOlVhjsDdKDI2K67OtCKo0TJm1f08FTiVrUHot2I6h29T1WrICWAg18aT7Dn3qGwhkw7DHcW+iGIK4JWcutfVBVXMegYe+ZCluFaMk6eD7mN2WfXOd63eYQUiuLU21g1gKNey9tQsE105pnsGwx1tVqUGhpjmi7jb+cS9kCPokhWrnO/JdEHjOGpAp69N0r2w9Zm471HlNLIsKXdntYc8T56xvIboGyxsHLNgrAVdq0MaEvtcODVkAfhNi2R3Y17gh7KR4G79s6gsD+vqb8js7jFBteGFNLPURVJ8jCKARMHKKyuAvQJhshecITJKuOWyWXSKIwDXoYyjS5NURssWKdjbZ16L+7EJtC6GtiSXxCC1xjAU/6U9Qe2xTXC/cJAEYu/eBgTkCuSqx1NWp7+jLvcl8C6PRFJil5PihsXMfex8TgbFsxu9Eg7aq0O8Qibgb+Fh6UkreAe7H+ZAYpxjBqKA58lnsD/uKIbqkn+uAUJiQyuw7G2nsIs9xH8RsvSHBttKKtWQBA8IdbEEcTDVFZXCKpfX5bTK69DDIPQexAAvEgjbFHduamkXRGGBJat/1bdGBORZ8yjNoNiaCGqQ1OiXLET3UCrtlX5PIgCKasY2qIlUOj4LASBX7xYhObRiKTVWM0xVuEm2nLWTAzY9EAk+b2BzxSkULdLfl3taSFa/ZozZMjn80pdJJpli80cY24IW94CRDm1Mb1Cr26G9XFN9pLwiEuBKhFkG10yY26Fn4TEwWeGo6T39NLOl8WysZOJlKUtC0lWboRC1SnNAsRig6DrnNEW6S7mAHqemul1urfhL9h2450hgRKpyy0W3o/Euv47Sz8YIHxtlZg4EwdKYUBgRH0kjY2CbpGG+TE0SV+BMlYUD74HZfn7LEIi3cga7aClLEbtl7JjYohMZmDT3mtW8cc/4OAAk483etUWcpmtwQnQm1qaIVmmlLgg0ZJD+LPWGz2C5uAL3c5p0PeaZrTAZSJaFUG/va1kKtADeccy6JmGJfWmXoWOa8DgiSTgLIqPamzRZJDYK4uJW6wLEiDv7NGPf5MW0DRiw2S7RBtRTVwz1QbTqlQZTUBGmhrrvOHDQHJ+fIZWkYkuM5R8hryhya7uVeYCImg+Q32fZELLDRxEgTKQAIVDoxdEOPC68PAFLjDshfgfHUKz0cBZ9jJ6sWUGEnpx3EJT1t52qYg+Nf41RS19McXiy2aY2LwQazrXJaTUehJ2KpbAIw4nxWKsNEqZuxG7bZPGpPfBB0hwot2tQ9vhiALeVrkTBM1nQ8nf5IEsQYDEdTsHNA1KY1mgKBlFcARa37swAKUfI8OSemRmWMjWJAYogOVVo4BAMspgIMwAUjYCyZ37b3kkjzgOrSKAGDOcYWY2qubjcIzf9UsBQvLG3a2JgkyyRIAOMbLc4ZH5t/IgHUk8NLOMT+TYJB7Slo0I4QnIexws9qqutQt8h9yCMWfDO+F62xSQ1oIlXsr39T7U1IPBGLehBjy4+Wk1KQ0hiL3hBJ1lkBCH/LYZC4eqq9MC4gw4KTLjWJQjT1RlMAOfkBXCpelXqNed37bhIXZqDSbQ7h3rBf8EPdH0zE4mNxWvPj0pQ/85PyM5FKBgyyiw0qnzYY3A8NIlYcUVryvFziOY7UH98E4gQi6s0BiwK3+W+0h3s2jVgKbIEfG9rtJIHMqcT6NqVELLpfDiVPzNnA7fTN+pE0QxfYRuZ4f0fUYHBMHd8w1h3QN8ay0Yw9ZRdJT5OvJZ3Pt8uljj2wIM5X3KQd/NwbPm0AvNznzdd7sfKxlkHJfKsPxzjeKzKUQPl11BDbAqkBGdSg+gIJwClNqInvxFdilw444IBGXwsBOc65T4VrqRoMOMcBXFPmkd8L4CkwivCenCLf8CIXuciOLhYrTL8EE8PHil+hKZw7NkpODcar9wAKIk39UbVTAAZbxRjz3/hcbcaYDZDEqx96KQmpPn7KSZtzESmewyfl5kRdo2gGvFA3QYvVhJcmXH8HhrAHuzW2itUuStwr4Kp20O5zkgANjj3XiPFFLL4VL5/zzv5AfLmkxO4RznPdqYd8Zau78mBzE6PveeyrMcehMVAsMFavH1ysAMDSj/WDFEFIBzxa5K7X37YNRARZ2IqdAFRUNvG3AIyxkoWB5Nv4T1GCJpBLx+dH2JFo10XRZD5G6oZKzg8h7lvM3f6dJBl/IGVjtGear7vDZq2Scku2qRyT3UQAAAqoSURBVOmsCAtOb45JKXjhNJAhYgCoxEFbU44coppFWnLGIln2lOUvCaNSRDhid2Q+YZsG7WaZWss/JwFpBuXlkUWngZxEQOPtINbKyVxSVU07CGWO99tvv1FHiGIAkkVVqfOD4BTMdL04um8BONpUc76PmHOMUHkJAiaRqW4qlxappylK32LUN7YpvxunMFwwEBeH9mCacpOxWG2kXnKIm040UbOgmnXMGeXEmP9AKkF4kRDhJyhnrBrkTohA5DksEmT8efkWg40oTa8aBEq4EGPe2DCFIF33Qn9igwF6mA0agwbJC4IWq9jZUvyvKW1Afal7H4OcwHYdUaVcAgFhB8xLkYwllugDRzdHSdIknF2+UzRRDja3qaSLg07aN6UyF+OIX0KvkT3A1EJ/0GFeDrhYGbElnd+UkJNb4cD2Ff83cQ3Jov5wCaRD7Qj9N4WGhnIsJKf0II+0SL8I7ubnYbSFoSyCsfjEKwCH9r1b19m4YQ9x/RBLkiX8l5+csFhB4SWqNh2SASqb4JjwEHunBI0a5a+xfaSLOhwbPYi3CHhWNOEsIaTcGLMB1O0cG/d2i0jxXMFtjF0vPjUH2eJ8p+hihc6WOLHJo0cobYzaYuDBUYFcwEBags3R+Rh0aRxCMgBKrhogxChOjXFCtlTlHOd57Dax4AJ2tL5zRKRFDNRbE6ItVpNagoljHdW2yShF41cBBOyMN/uQND7RWGKxRV5NmKtl9gc6xIlhjPXBoSTVm96EmghFPRVFNXKM80B0esuP+NrczTPF4jivYDzOYfQNqv6WgSF9M8RyWexrXY0CF1Q5idMHwyw2OPSl2EP6361rxFCZoPr2XaYDc+f+72LlX6V81twNCmTISZZm8zdxF9cbI1kMMYe26dBlsU3QnS8HxPD8OZp9G+3mnvOY5yEKP7GOuM0Vc+al1IsV6ljuRv231AXEFfWDyqv4W2KDY4gF9nMWm3ahyBgDE/pgw0QumqIXYxZzN+9RVUYNmlMdxHGb2K18HouV47UseU/90MHzs0iTzQ7UobQE1cjwj0GXUjbua4o8mJDafKoWugJEdoMBh8596HXsrfFap3oT38SEsiGR/0uvZGrbDzS006briHec7U73slXihHJMY96AI3IfJ9XU+5P2YKSlc/RlP/Om78w3B2E+ewqagspsb1RnRQRpsUIdySmes1k4+lbKXaekQkCY3yb5WEos9wldmVRTBkB2gANucsAMR7mrUnfOuU55lhpBJqFtjxngJCUUxFys8k7LPCIwpfP8XmkXRFK/TaIYUO6B1EWpU4z7qIy2GCW7aEuP4hiFOqLWm35AibXC1F0+LPPEz4ozpxYrTlzOdRRqTiy7Evk5nGPizkO3U4KfVPqKPTaPMW6zdWoYpGTU2onKk0LB3r7FmIsxd+s5kKBIjPSJtlid4rzcjQg022GRcTmJEiwWyZBSn7KxvGlhpGKEmwANwV59yiZvO7HEOyHcqMVYrEJNqWBm7iaCwc/B5fwrWVCFM1IxpTarbWxBDNJEknGiaImIBvUxx8kAc69L3/NiTgCaNYRq4wjWxSpmtxS3mzuOZvE4xVLx4oO4gwRIsPUdjtU3ofx3gVxEAnFFAtgAgEO4hmrcxsYGi7rvv//+O7Ihi1WZ9FJmVeR6Sj1ffVGEURTNKEUTfWCn/FtGdEzKpWnR2UB7izEa4BJciSMl7hT7kLC5JHm3Cc9MQNE0EjvP7uawPiUfqSl2RKl0XsswZXAIpUM7Hu2XjZCKvNkUYlFz0i/SCpKaahHVXDhsJRrGi+McqBPFOqL1Y5KoU9ZgyL0Y2VzYWS6KrARAIZPApdqRfPRS6dg7pSQKGIBAAIKxe7MMEgrUufAP+C6mJ9UODTZt0emaGPXJDTAhk1ODKIoPGEGCfKucWJxk2WNQ3vVsGlUs5YBZ3CdTLBx2YDYuCN/KeggnOU3AGsXJO/mxsNS60FyeZ1ysnMellH4UsrAB4lU4k8PmYjYGhS3y0Akq8qRWDZCaFWYiAXy6ttI2DrQwi75Ju8lIE/henaDybp9crUGdiJhXTdnkjVh59tg4EBbI8WzPZLgREFP6UNWk3rzNU5rdWNvGG/k+boXnSQnJjPuw1T7GbyOFALPMAZuqH26NSLu/Tc+3i0QJRF7fuFgdn7AUBMWpeYs39OBIAVQdGxTJQwR6VcKS+vRvE/OJN4lSP5CggVj8qOvDGJziOHKBWjMJ/Zmw51soKgv3yy535aUQi9TlBTOKaBC666XSoiukHlFJn/kZkw9ut6iI4WMOuRuQ/2bNuCiasbsu1sd6mIdUDUbHcENVsUyIEohcuy1WhRlLSG3IVpzgGEUnijdxi8iCxbbQJukvopqAwfv4DmFN2vc410SC2P6Pw02k1J5JwYgb5jsqRasRa8rB+8YcH2P2Mb9o5mU+COcvh92cuqIzJX6fDRTcnD2IRQWO3Yd1YOr8pr4QKzbpxe8qXEnMXK9r3xtzhHIRK5fExcowLxl/OzO2salhkOvKq6ZEMhjyKZK1t9eCZAEYNE60lCm2jX9bicUtYHtyYtliyw5tM7HYXT5ibha2nlgy0uzT/5pk/U8SS4ITsfLyb5IFgW5DtrhN3bYSC2zvegHL3tbfXf0jFr8pP1eWQ0k1Itq2NrtlVOTusFmrurSl8MbUY3r21qJQgyICebbbwfv8w20mFqeY75gnXJOfpbpmSg363iKUfkX3hW7yYyGoENGSbahwals7VdKItQO6r3JZKdy0rcQSyiJF+RE/iMX32mbJkkwVQttBrFUl61IB/LaqQbUX1GBe2M+hFNXYZslSoqDqbEcEY1Wem/ZnbatkIRaVl7/60JtWBWy3mViOWbAbNc9QpKi75F3Tzse9aYuG9o1YArn1fJbIxjarwUZirVLiSymQscf+DF3U3boOsZRl57stvHNZmqd+XMRujWE3nivzrdwiPyJ2scpILunFTX6TQNdiOOlMniov+pFslE3eZslSnYUB8/ObFiufJG0AH3s4yW5wVckzSZacVn4snfoPxZ5dR7KW9LE3rrXVh2rfkdZfZVWXMq+cyik7HffGhPQJuiNYvo+JVCk82WY1CAkqqcsPY1msdj0uRQGU6kIec++A3C0iStOrzQDTJf3yFL7vEQ9X2nXiwy6LBow5LWe35uC5EreSueKb/EUOvpSPbLNd/Plr2xerHflLUFdaG6FUDPkwbD7qBWR09wYREUT6XQ2DlId4nw9iqNWQpVWjYINeHPgR2Vj3rqqNk3Ns8rLarqfyxdt8zI1WkYbwfZ6Ol/EtrcnXtyy5EgXlAcbgo3TBPPyNugwEMi8Zd/cph5D+x1TeMaawp85YO95MZxHsxuBkWpyoR0AoD/OJXQ/qJKSy/T9+i/qMqFHIOdJ3UVgiRR71DiaGu0zKghq8/ysXiGMQoi7DAiOO2gyTwkxDax09y/PNy8exEYpZlCro2+8WLZjSeH0QzN8oUajPyX1apP79jdqNMCsk3zOsGebwiVfYK9Xz7yESn4jVZatiEXEB7jBhf2OBo9BFfULUKcRE6+rD78GxCG2hfXyH4CYRL0YLbg+O71JFJbUNfc8JaYiakiAG5orCmHiGeRp3ENWc4uP7IQTIx9M3j7Vk7aZe3vfseVZgH7HmWccD5Sn/B2ukReBcPlpEAAAAAElFTkSuQmCC\" alt=\"\">");
			this.setRowFieldText(detail, "commodity.name", "S16VKNI00247");
			this.setRowFieldText(detail, "commodity.color", "黑色");
			this.setRowFieldText(detail, "commodity.supplyType", "生产");
			this.setRowFieldText(detail, "XS", "52");
			this.setRowFieldText(detail, "S", "112");
			this.setRowFieldText(detail, "M", "162");
			this.setRowFieldText(detail, "L", "142");
			this.setRowFieldText(detail, "XL", "52");
			this.setRowFieldText(detail, "amount", "520");
			this.setRowFieldText(detail, "cprice", "100");
			this.setRowFieldText(detail, "cmoney", "52000");
		}
		this.onMenu("提交");
		if ("安排去生产".length()>0)
			new ArrangeTicketTest().check订单安排__1用常规库存_2请购_3直发_4当地购('2', number);
		if ("给订单明细配置BOM".length()>0)
			this.set配置BOM_物料计算_生产开单_生产录入(number);
		if ("生产收货".length()<0)
			this.set生产收货_发货_回款(number);
		return number;
	}
	
	private void set配置BOM_物料计算_生产开单_生产录入(String number) {
		PPurchaseTicketTest test = new PPurchaseTicketTest();
		PPurchaseTicketForm form = (PPurchaseTicketForm)test.getForm();
		ReflectHelper.copyProperties(this, test);
		if ("1设置订单物料BOM，已有Bom则退出".length()>0) {
			this.loadSql("CommonList", "selectedList", "number", number);
			Assert.assertTrue("到生产开单列表", this.getListViewValue().size()>0);
			for (String monthnum: (List<String>)(List)this.getListViewColumn("monthnum")) {
				this.setFilters("monthnum", monthnum);
				this.setSqlListSelect(1);
				this.onMenu("配置BOM");
				BOMForm bomForm = form.getBomForm();
				if ("已有Bom则退出".length()>0 && bomForm.getDetailList().size()>0 && bomForm.getDetailList().get(0).getId()>0)
					return;
				bomForm.getDetailList().clear();
				bomForm.getShowList().clear();
				this.getEditListView().update();
				bomForm.getDomain().setAmount(1);
				bomForm.getDomain().setLevel(10);
				this.onButton("调整显示级数");
				StringBuffer sb = new StringBuffer();
				if (true) {
					this.onMenu("新增同级");
					BomDetail bomDetail = bomForm.getDetailList().get(0);
					this.setRowFieldText(bomDetail, "commodity.name", "调样颜色");
					this.setRowFieldText(bomDetail, "arrange", "去请购");
					this.setRowFieldText(bomDetail, "commodity.supplyType", "生产");
					this.setRowFieldText(bomDetail, "aunit", "1");
					sb.append(bomDetail).append(bomDetail.getCommodity().getName()).append(bomDetail.getBomTicket().getAunit()).append("\n");
				}
				if (true) {
					this.setEditListSelect(1);
					this.onMenu("新增同级");
					BomDetail bomDetail = bomForm.getDetailList().get(1);
					this.setRowFieldText(bomDetail, "commodity.name", "开裁");
					this.setRowFieldText(bomDetail, "arrange", "去请购");
					this.setRowFieldText(bomDetail, "commodity.supplyType", "生产");
					this.setRowFieldText(bomDetail, "aunit", "1");
					sb.append(bomDetail).append(bomDetail.getCommodity().getName()).append(bomDetail.getBomTicket().getAunit()).append("\n");
				}
				if (true) {
					this.setEditListSelect(2);
					this.onMenu("新增子级");
					BomDetail bomDetail = bomForm.getDetailList().get(2);
					this.setRowFieldText(bomDetail, "commodity.name", "门幅160cm，克重250左右，成分90涤纶10氨纶");
					this.setRowFieldText(bomDetail, "arrange", "去请购");
					this.setRowFieldText(bomDetail, "commodity.supplyType", "采购");
					this.setRowFieldText(bomDetail, "remark", "大货面料净幅宽160CM-150CM都可以大货价格克重待定");
					this.setRowFieldText(bomDetail, "aunit", "3");
					sb.append(bomDetail).append(bomDetail.getCommodity().getName()).append(bomDetail.getBomTicket().getAunit()).append("\n");
				}
				if (true) {
					this.setEditListSelect(1);
					this.onMenu("新增同级");
					BomDetail bomDetail = bomForm.getDetailList().get(3);
					this.setRowFieldText(bomDetail, "commodity.name", "上线");
					this.setRowFieldText(bomDetail, "arrange", "去请购");
					this.setRowFieldText(bomDetail, "commodity.supplyType", "生产");
					this.setRowFieldText(bomDetail, "aunit", "1");
					sb.append(bomDetail).append(bomDetail.getCommodity().getName()).append(bomDetail.getBomTicket().getAunit()).append("\n");
				}
				if (true) {
					this.setEditListSelect(1);
					this.onMenu("新增同级");
					BomDetail bomDetail = bomForm.getDetailList().get(4);
					this.setRowFieldText(bomDetail, "commodity.name", "下线");
					this.setRowFieldText(bomDetail, "arrange", "去请购");
					this.setRowFieldText(bomDetail, "commodity.supplyType", "生产");
					this.setRowFieldText(bomDetail, "aunit", "1");
					sb.append(bomDetail).append(bomDetail.getCommodity().getName()).append(bomDetail.getBomTicket().getAunit()).append("\n");
				}
				if (true) {
					this.setEditListSelect(5);
					this.onMenu("新增子级");
					BomDetail bomDetail = bomForm.getDetailList().get(5);
					this.setRowFieldText(bomDetail, "commodity.name", "1.1CM树脂扣");
					this.setRowFieldText(bomDetail, "arrange", "去请购");
					this.setRowFieldText(bomDetail, "commodity.supplyType", "采购");
					this.setRowFieldText(bomDetail, "aunit", "6");
					sb.append(bomDetail).append(bomDetail.getCommodity().getName()).append(bomDetail.getBomTicket().getAunit()).append("\n");
				}
				if (true) {
					this.setEditListSelect(1);
					this.onMenu("新增同级");
					BomDetail bomDetail = bomForm.getDetailList().get(6);
					this.setRowFieldText(bomDetail, "commodity.name", "整烫");
					this.setRowFieldText(bomDetail, "arrange", "去请购");
					this.setRowFieldText(bomDetail, "commodity.supplyType", "生产");
					this.setRowFieldText(bomDetail, "aunit", "1");
					sb.append(bomDetail).append(bomDetail.getCommodity().getName()).append(bomDetail.getBomTicket().getAunit()).append("\n");
				}
				if (true) {
					this.setEditListSelect(1);
					this.onMenu("新增同级");
					BomDetail bomDetail = bomForm.getDetailList().get(7);
					this.setRowFieldText(bomDetail, "commodity.name", "包装和装箱");
					this.setRowFieldText(bomDetail, "arrange", "去请购");
					this.setRowFieldText(bomDetail, "commodity.supplyType", "生产");
					this.setRowFieldText(bomDetail, "aunit", "1");
					sb.append(bomDetail).append(bomDetail.getCommodity().getName()).append(bomDetail.getBomTicket().getAunit()).append("\n");
				}
//				LogUtil.error(sb.toString());
				new StoreTicketTest().setQ清空();
				this.onMenu("提交");
				this.setFilters("monthnum", monthnum);
				test.setBomDetailsShow();
			}
		}
if (true)	return;
		if ("原物料采购,面料,扣子".length()>0) {
			PurchaseTicketTest purTest = new PurchaseTicketTest();
			purTest.loadSql("CommonList", "selectFormer4Bom.selectedList", "number", number);
			HashSet<Object> commNames = new HashSet<Object>(purTest.getListViewColumn("commName"));
			for (Object name: commNames) {
				purTest.check原物料普通采购__1采购_2同商品采购('2', number, "commName", name);
			}
			if ("原物料采购收货,面料,扣子".length()>0) {
				for (Object name: commNames) {
					new ReceiptTicketTest().check收货开单__1全数收货_2部分收货n拆单('1', number, "commName", name);
				}
			}
		}
		if ("生产前物料计算".length()>0)
			test.check生产开单_1设置Bom_2物料计算_3拆分('2', number);
		if ("1生产开单".length()>0) {
			this.loadSql("CommonList", "selectedList", "number", number);
			int detailCount = this.getListViewValue().size();
			Assert.assertTrue("订单明细未到生产开单", detailCount>0);
			this.setSqlAllSelect(2);
			this.onMenu("开单");
			this.onButton("生成单号");
			this.setFieldText("purDate", "2024-3-21");
			int price = 80;
			for (OrderDetail pur: form.getDetailList())
				pur.setPrice(++price);
			this.onMenu("提交");
		}
		if ("生产录入".length()>0) {
			this.loadView("RecordList", this.genFiltersStart(new Object[0], "number", number));
			test.setBomDetailsShow();
			if ("标准的生产录入".length()>0) {
				for (Hyperlink link: form.getMonthnumLinkList()) {
					if (link.getComponent()==null)
						continue;
					LinkedHashSet<BomDetail> bomList = new LinkedHashSet<BomDetail>();
					for (TextField tf: link.getInnerComponentList(TextField.class)) {
						if (tf.getFormer()==null)
							continue;
						BomDetail bom = (BomDetail)tf.searchFormerByClass(Field.class).getFieldBuilder().getEntityBean().getBean();
						bomList.add(bom);
					}
					for (BomDetail bom: bomList) {
						if ("生产物料，有生产数量".length()>0 && new SupplyTypeLogic().isProductType(bom.getCommodity().getSupplyType()))
							this.setEntityFieldText(bom, "commitAmount", bom.getAmount());
						this.setEntityFieldText(bom, "occupy1", bom.getAmount());
					}
				}
			}
			this.onMenu("保存录入");
		}
	}
	
	private void set生产收货_发货_回款(String number) {
		if ("生产收货".length()>0) {
			ReceiptTicketTest test = new ReceiptTicketTest();
			test.check收货开单__1全数收货_2部分收货n拆单('1', number);
		}
		if ("生产发货".length()>0) {
			SendTicketTest test = new SendTicketTest();
			test.check发货开单__1全发_2部分发('1', number, 0);
		}
		if ("收款".length()>0) {
			SendTicketTest test = new SendTicketTest();
			test.check发货回款__1全额支付_2部分支付80('1', number);
		}
	}

	@Override
	protected void setQ清空() {
		String sticket = "delete from sa_OrderTicket where sellerId=?";
		String sdetail = "delete from sa_OrderDetail where sellerId=?";
		String scount = "delete from sa_OrderCount where sellerId=?";
		String sbom = "delete from sa_BomDetail where sellerId=?";
		String sEnough = "delete from sa_StoreEnough where sellerId=?";
		String sStore = "delete from sa_StoreItem where sellerId=?";
		SSaleUtil.executeSqlUpdate(sticket, sdetail, scount, sbom, sEnough, sStore);

		String sPrint = "delete from bs_PrintViewSerial where sellerId=?";
		SSaleUtil.executeDerbyUpdate(sPrint);
	}
}
