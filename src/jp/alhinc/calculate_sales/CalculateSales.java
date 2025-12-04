package jp.alhinc.calculate_sales;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalculateSales {

	// 支店定義ファイル名
	private static final String FILE_NAME_BRANCH_LST = "branch.lst";

	// 支店別集計ファイル名
	private static final String FILE_NAME_BRANCH_OUT = "branch.out";

	//商品定義ファイル名
	private static final String FILE_NAME_COMMODITY_LST = "commodity.lst";

	//商品別集計ファイル名
	private static final String FILE_NAME_COMMODITY_OUT = "commodity.out";


	// エラーメッセージ
	private static final String UNKNOWN_ERROR = "予期せぬエラーが発生しました";
	private static final String FILE_NOT_EXIST = "定義ファイルが存在しません";
	private static final String FILE_INVALID_FORMAT = "定義ファイルのフォーマットが不正です";
	private static final String FILE_NOT_SEQUENTIAL = "売上ファイル名が連番になっていません";
	private static final String SALEAMOUNT_EXCEEDS_LIMIT = "合計金額が10桁を超えました";
	private static final String SALES_FILE_INVALID_FORMAT = "のフォーマットが不正です";
	private static final String BRANCH_CODE_NOT_FOUND = "の支店コードが不正です";
	private static final String COMMODITY_CODE_NOT_FOUND = "の商品コードが不正です";

	//支店コードの正規表現
	private static final String BRANCH_REGEX = "^[0-9]{3}$";
	//商品コードの正規表現
	private static final String COMMODITY_REGEX = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).{8}$";

	/**
	 * メインメソッド
	 *
	 * @param コマンドライン引数
	 */
	public static void main(String[] args) {
		// 支店コードと支店名を保持するMap
		Map<String, String> branchNames = new HashMap<>();
		// 支店コードと売上金額を保持するMap
		Map<String, Long> branchSales = new HashMap<>();
		// 商品コードと商品名を保持するMap
		Map<String, String> commodityNames = new HashMap<>();
		// 商品コードと売上金額を保持するMap
		Map<String, Long> commoditySales = new HashMap<>();
		//コマンドライン引数が渡されているかチェック
		if (args.length != 1) {
			System.out.println(UNKNOWN_ERROR);
			return;
		}
		// 支店定義ファイル読み込み処理
		if(!readFile(
			args[0],
			FILE_NAME_BRANCH_LST,
			branchNames,
			branchSales,
			BRANCH_REGEX,
			"支店"
		)) {
			return;
		}
		// 商品定義ファイル読み込み処理
		if(!readFile(
			args[0],
			FILE_NAME_COMMODITY_LST,
			commodityNames,
			commoditySales,
			COMMODITY_REGEX,
			"商品"
		)) {
			return;
		}
		//(処理内容2-1、2-2)
		//全てのファイルを取得し、配列filesに格納
		File[] files = new File(args[0]).listFiles();
		//ファイルを対象とし、ファイル名が数字8桁で、拡張子が rcdのものを抽出してrcdFilesに格納
		List<File> rcdFiles = new ArrayList<>();
		for(int i = 0; i < files.length; i++) {
			if(files[i].isFile() && files[i].getName().matches("^[0-9]{8}[.]rcd$")) {
				rcdFiles.add(files[i]);
			}
		}
		//売上ファイルが連番かチェック
		Collections.sort(rcdFiles);
		for(int i = 0; i < rcdFiles.size() - 1; i++) {
			String formerName = rcdFiles.get(i).getName();
			String latterName = rcdFiles.get(i + 1).getName();
			int former = Integer.parseInt(formerName.substring(0, 8));
			int latter = Integer.parseInt(latterName.substring(0, 8));
			if((latter - former) != 1) {
				System.out.println(FILE_NOT_SEQUENTIAL);
				return;
			}
		}
		//rcdFilesを要素数の数だけ繰り返す
		for(int i = 0; i < rcdFiles.size(); i++) {
			//rcdFilesの中身を読み込むリストfileContentsを作成
			List<String> fileContents = new ArrayList<>();
			BufferedReader br = null;
			try {
				br = new BufferedReader(new FileReader(rcdFiles.get(i)));
				String line;
				while((line = br.readLine()) != null) {
					fileContents.add(line);
				}
				//売上ファイルのフォーマットをチェック
				if(fileContents.size() != 3) {
					System.out.println(rcdFiles.get(i).getName() + SALES_FILE_INVALID_FORMAT);
					return;
				}
				//Mapに特定のKeyが存在するかチェック
				if (!branchNames.containsKey(fileContents.get(0))) {
					System.out.println(rcdFiles.get(i).getName() + BRANCH_CODE_NOT_FOUND);
					return;
				}
				if (!commodityNames.containsKey(fileContents.get(1))) {
					System.out.println(rcdFiles.get(i).getName() + COMMODITY_CODE_NOT_FOUND);
					return;
				}
				//売上金額が数字なのかチェック
				if(!fileContents.get(2).matches("^[0-9]*$")) {
					System.out.println(UNKNOWN_ERROR);
					return;
				}
				long fileSale = Long.parseLong(fileContents.get(2));
				long branchSaleAmount = branchSales.get(fileContents.get(0)) + fileSale;
				long commoditySaleAmount = commoditySales.get(fileContents.get(1)) + fileSale;
				//売上金額の合計が10桁を超えていないかチェック
				if(branchSaleAmount >= 10000000000L && commoditySaleAmount >= 10000000000L){
					System.out.println(SALEAMOUNT_EXCEEDS_LIMIT);
					return;
				}
				branchSales.put(fileContents.get(0), branchSaleAmount);
				commoditySales.put(fileContents.get(1), commoditySaleAmount);
			} catch(IOException e) {
				System.out.println(UNKNOWN_ERROR);
				return;
			} finally {
				// ファイルを開いている場合
				if(br != null) {
					try {
						// ファイルを閉じる
						br.close();
					} catch(IOException e) {
						System.out.println(UNKNOWN_ERROR);
						return;
					}
				}
			}
		}
		// 支店別集計ファイル書き込み処理
		if(!writeFile(args[0], FILE_NAME_BRANCH_OUT, branchNames, branchSales)) {
			return;
		}
		if(!writeFile(args[0], FILE_NAME_COMMODITY_OUT, commodityNames, commoditySales)) {
			return;
		}
	}


	/**
	 * 支店定義ファイル読み込み処理
	 *
	 * @param フォルダパス
	 * @param ファイル名
	 * @param 支店コードと支店名を保持するMap
	 * @param 支店コードと売上金額を保持するMap
	 * @param 商品コードと商品名を保持するMap
	 * @param 商品コードと売上金額を保持するMap
	 * @return 読み込み可否
	 */
	private static boolean readFile(
		String path,
		String fileName,
		Map<String, String> names,
		Map<String, Long> sales,
		String regex,
		String error
	) {
		BufferedReader br = null;
		try {
			File file = new File(path, fileName);
			//ファイルの存在チェック
			if(!file.exists()) {
				System.out.println(error + FILE_NOT_EXIST);
				return false;
			}
			FileReader fr = new FileReader(file);
			br = new BufferedReader(fr);
			String line;
			// 一行ずつ読み込む
			while((line = br.readLine()) != null) {
				//(処理内容1-2)
				//支店コードと支店名、及び商品コードと商品名を保持する
				String[] items = line.split(",");
				//支店定義ファイル及び商品定義ファイルのフォーマットチェック
				if((items.length != 2) || (!items[0].matches(regex))){
					System.out.println(error + FILE_INVALID_FORMAT);
					return false;
				}
				names.put(items[0], items[1]);
				sales.put(items[0], 0L);
			}

		} catch(IOException e) {
			System.out.println(UNKNOWN_ERROR);
			return false;

		} finally {
			// ファイルを開いている場合
			if(br != null) {
				try {
					// ファイルを閉じる
					br.close();
				} catch(IOException e) {
					System.out.println(UNKNOWN_ERROR);
					return false;
				}
			}
		}
		return true;
	}


	/**
	 * 支店別集計ファイル書き込み処理
	 *
	 * @param フォルダパス
	 * @param ファイル名
	 * @param 支店コードと支店名を保持するMap
	 * @param 支店コードと売上金額を保持するMap
	 * @param 商品コードと商品名を保持するMap
	 * @param 商品コードと売上金額を保持するMap
	 * @return 書き込み可否
	 */
	private static boolean writeFile(
		String path,
		String fileName,
		Map<String, String> names,
		Map<String, Long> sales
	) {
		//(処理内容3-1)
		BufferedWriter bw = null;
		try {
			File writefile = new File(path, fileName);
			bw = new BufferedWriter(new FileWriter(writefile));
			for (String key : names.keySet()) {
				bw.write(key + "," + names.get(key) + "," + sales.get(key));
				bw.newLine();
			}

		} catch(IOException e) {
			System.out.println(UNKNOWN_ERROR);
			return false;

		} finally {
			// ファイルを開いている場合
			if(bw != null) {
				try {
					// ファイルを閉じる
					bw.close();
				} catch(IOException e) {
					System.out.println(UNKNOWN_ERROR);
					return false;
				}
			}
		}
		return true;
	}

}
