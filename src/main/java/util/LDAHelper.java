package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import com.huaban.analysis.jieba.JiebaSegmenter;
import com.huaban.analysis.jieba.SegToken;

import jgibblda.Inferencer;
import jgibblda.LDACmdOption;
import jgibblda.Model;


public class LDAHelper {
	public String model_prefix;
	public static Inferencer inferencer;
	public static HashMap<Integer,Integer> cate_index_map;
	public static JiebaSegmenter segmenter = new JiebaSegmenter();
	
	public LDAHelper(String model_prefix, String cate_index_file)
	{
		this.model_prefix = model_prefix;
		LDACmdOption option = new LDACmdOption();
		
		//set up option parameter
		option.modelName = model_prefix;
		option.predict = true;
		option.inf = true;
		option.dfile = "./";
		option.dir = ".";
		
        try {
			inferencer = new Inferencer(option);
			cate_index_map = loadCateIndexFile(cate_index_file);
			//System.out.println("cate_index_map="+cate_index_map);
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
	
	private HashMap<Integer, Integer> loadCateIndexFile(String cate_index_file) {
		HashMap<Integer, Integer>  cate_index_map = new HashMap<Integer,Integer>();
		File cateIndexFile = new File(cate_index_file);
		FileReader ctrFileReader = null;
			try {
				ctrFileReader = new FileReader(cateIndexFile);
				BufferedReader br = new BufferedReader(ctrFileReader);
				
				String s1 = null;
				String[] cols = null;
				 
				while((s1 = br.readLine()) != null) {
					cols = s1.split("\t");
					if(cols.length==2)
					{
						try
						{
							cate_index_map.put(Integer.parseInt(cols[1]), Integer.parseInt(cols[0]));
							
						}
						catch(Exception exp)
						{
							exp.printStackTrace();
						}
					}
				}
			}
			catch(Exception exp)
			{
				exp.printStackTrace();
			}
			return cate_index_map;
	}

	public static synchronized Model predict(String str)
	{
		try {
			//System.out.println("cate distribution for "+str);
			return inferencer.predict(str);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} 
	}
	
	public static HashMap<Integer,Double> get_cate_distribution_map(double[][] theta)
	{
		HashMap<Integer, Double> cate_distribution_map = new HashMap<Integer, Double>();
		double[] cate_scores = theta[0];
		int len = cate_scores.length;
		for(int i = 0; i < len ; i++)
		{
			cate_distribution_map.put(cate_index_map.get(i+1), cate_scores[i]);
		}
		return cate_distribution_map;
	}

	public static List<Map.Entry<Integer,Double>> get_sorted_cate_distribution_map(double[][] theta)
	{
		final HashMap<Integer,Double> cate_distribution_map = get_cate_distribution_map(theta);
		//System.out.println(cate_distribution_map);
		ArrayList keys = new ArrayList(cate_distribution_map.keySet());
        Collections.sort(keys,new Comparator<Integer>(){
            public int compare(Integer o1,Integer o2){
            	//按照value的值降序排列，若要升序，则这里小于号换成大于号
                if(cate_distribution_map.get(o1)>cate_distribution_map.get(o2))
                    return -1;
                else if(cate_distribution_map.get(o1)<cate_distribution_map.get(o2))
                    return 1;
                else
                    return 0;
                }
            }
      );
        for(Iterator<Integer> it=keys.iterator();it.hasNext();)  
        {  
        	int index = it.next();
            //System.out.println(index+":"+cate_distribution_map.get(index));  
        }  
        return keys;
	}
	
	public static String segment(String str)
	{
		String segment_result = "";
		
		List<SegToken> segs = segmenter.process(str, JiebaSegmenter.SegMode.SEARCH);
		for (SegToken seg : segs)
		{
			segment_result+=" "+seg.word.getToken();
		}
		return segment_result;
	}
	
	public static void main(String[] args)
	{
		LDAHelper h = new LDAHelper(args[0],args[1]);
		BufferedReader buf = null;  
        
        String str = null;  
        //System.out.println("Please input");  
        buf = new BufferedReader(new InputStreamReader(System.in));
        try {
	        while( (str = buf.readLine()) != null)
	        {
	        	String seg_str = segment(str);
	        	System.out.println(str+"\t"+seg_str+"\t"+parseModel(h.predict(seg_str)));
					
	        }
        } catch (Exception e) {
			e.printStackTrace();
		}
		/*
		LDAHelper h = new LDAHelper("model","poi.segment.txt.cate_index");
		
		System.out.println(parseModel(h.predict("KTV")));
		System.out.println(parseModel(h.predict("肯德基")));
		 //boolean init_success = inferencer.predict_init();
        BufferedReader buf = null;  
            
        String str = null;  
        System.out.println("Please input");  
        while( (buf = new BufferedReader(new InputStreamReader(System.in))) != null)
        {
        	try {
				str = buf.readLine();
				System.out.println(parseModel(h.predict(segment(str))));
			} catch (Exception e) {
				e.printStackTrace();
			}
        }*/
	}
	
	public static HashMap<Integer,Double> get_cate_distribution_map(String str)
	{
		return get_cate_distribution_map(predict(segment(str)).theta);
	}

	public static String parseModel(Model m) {
		String cate_dist_str = "";
		//System.out.println(Arrays.toString(m.theta[0]));
		HashMap<Integer,Double> cate_distribution_map = get_cate_distribution_map(m.theta);
		List<Map.Entry<Integer,Double>> result = get_sorted_cate_distribution_map(m.theta);
		//System.out.println(result);
		for(int i = 0; i < result.size(); i++)  
		{
			int index = Integer.parseInt(result.get(i)+"");
			String weight_str = cate_distribution_map.get(index)+"";
			if(weight_str.length()>10)
			{
				weight_str = weight_str.substring(0,9);
			}
			cate_dist_str = cate_dist_str + index+":"+weight_str+" ";
		}
		return cate_dist_str;
	}
}
