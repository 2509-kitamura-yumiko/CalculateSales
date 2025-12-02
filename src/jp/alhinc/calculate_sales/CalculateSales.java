package jp.alhinc.calculate_sales;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalculateSales {

	// 支店定義ファイル名
	private static final String FILE_NAME_BRANCH_LST = "branch.lst";

	// 支店別集計ファイル名
	private static final String FILE_NAME_BRANCH_OUT = "branch.out";

	// エラーメッセージ
	private static final String UNKNOWN_ERROR = "予期せぬエラーが発生しました";
	private static final String FILE_NOT_EXIST = "支店定義ファイルが存在しません";
	private static final String FILE_INVALID_FORMAT = "支店定義ファイルのフォーマットが不正です";

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

		// 支店定義ファイル読み込み処理
		if(!readFile(args[0], FILE_NAME_BRANCH_LST, branchNames, branchSales)) {
			return;
		}

		// ※ここから集計処理を作成してください。(処理内容2-1、2-2)
		//全てのファイルを取得し、配列filesに格納
		String path = args[0];
		File[] files = new File(path).listFiles();

		//filesに存在するすべてのファイルから、
		//ファイル名が数字8桁で、拡張子が rcdのものを抽出してrcdFilesに格納
		List<File> rcdFiles = new ArrayList<>();

		for(int i = 0; i < files.length ; i++) {
			if(files[i].getName().matches("^[0-9]{8}.rcd$")) {
				rcdFiles.add(files[i]);
			}
		}


		//rcdFilesの中身を読み込むリストfileSaleを作成
		List<String> fileSales = new ArrayList<>();
		//rcdFilesを要素数の数だけ繰り返す
		for(int i = 0; i < rcdFiles.size() ; i++) {

			File f = rcdFiles.get(i);
			BufferedReader br = null;

			try {
				br = new BufferedReader(new FileReader(f));
				String line;

				while((line = br.readLine()) != null) {
				fileSales.add(line);
				}

			} catch(IOException e) {
				System.out.println(UNKNOWN_ERROR);
			} finally {
				// ファイルを開いている場合
				if(br != null) {
					try {
						// ファイルを閉じる
						br.close();
					} catch(IOException e) {
						System.out.println(UNKNOWN_ERROR);
					}
				}
			}
		}
		for(int i = 1; i < fileSales.size(); i += 2) {
			String branchCode = fileSales.get(i - 1);
			long filesale = Long.parseLong(fileSales.get(i));
			long total = branchSales.get(branchCode) + filesale;
			branchSales.put(branchCode, total);
		}





		// 支店別集計ファイル書き込み処理
		if(!writeFile(args[0], FILE_NAME_BRANCH_OUT, branchNames, branchSales)) {
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
	 * @return 読み込み可否
	 */
	private static boolean readFile(String path, String fileName, Map<String, String> branchNames, Map<String, Long> branchSales) {
		BufferedReader br = null;

		try {
			File file = new File(path, fileName);
			FileReader fr = new FileReader(file);
			br = new BufferedReader(fr);

			String line;
			// 一行ずつ読み込む
			while((line = br.readLine()) != null) {
				// ※ここの読み込み処理を変更してください。(処理内容1-2)
				//支店コードと支店名を保持する
				String[] items = line.split(",");
				 branchNames.put(items[0], items[1]);
				 branchSales.put(items[0], 0L);

				System.out.println(line);
			}

		} catch(IOException e) {
			System.out.println(FILE_NOT_EXIST);
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
	 * @return 書き込み可否
	 */
	private static boolean writeFile(String path, String fileName, Map<String, String> branchNames, Map<String, Long> branchSales) {
		// ※ここに書き込み処理を作成してください。(処理内容3-1)

		BufferedWriter bw = null;

		try {
			File f = new File(path, "branch.out");
			bw = new BufferedWriter(new FileWriter(f));

			for (String key : branchNames.keySet()) {
				bw.write(key + "," + branchNames.get(key) + "," + branchSales.get(key));
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
