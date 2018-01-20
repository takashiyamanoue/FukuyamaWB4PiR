package org.yamaLab.pukiwikiCommunicator;
import java.util.StringTokenizer;

import org.rosuda.JRI.REXP;
import org.rosuda.JRI.Rengine;

import org.yamaLab.pukiwikiCommunicator.language.InterpreterInterface;
import org.yamaLab.pukiwikiCommunicator.language.LispObject;
import org.yamaLab.pukiwikiCommunicator.language.Util;

public class Rprogram implements InterpreterInterface{
	String rProgram;
	public LispObject avaluatingList;
	Rengine engine=null;
	boolean running;
	String output;
	public Rprogram(){
//		engine=new REngine(new String[]{"--no-save"},false,null);
		engine=new Rengine(new String[]{"--no-save"},false,null);
		output= "";
	}
	
	public String getOutputText(){
		return null;
	}
	
	public boolean isTracing(){
		return false;
	}
	/*****
	 * Rengine engine=new Rengine(....);
	 */
	
	private void runProgram(String x){
		StringTokenizer st=new StringTokenizer(rProgram,"\n");
		while(st.hasMoreTokens()){
			String line=st.nextToken();
			int pand = line.indexOf(";;");
			while(pand>=0){
				String line2=line.substring(0,pand-1);
				line=st.nextToken();
				line=line2+" \n "+line;
				//line=st.nextToken();
				pand=line.indexOf(";;");
			}
			//String line = "abc<-function(a){
			//  n<- c()
			//  for(i in 1:10){
			//       if(a[i] > 15.0){
			//           n<-c(n,1.0)
			//         } else{
			//               n<-c(n,0,0)
		    //           }
			//   }
			//   return(n)
			//}
			REXP result = engine.eval(line);
			System.out.println(result);
			output=output+result+"\n";
		}
	}
	
	boolean logging=true;	
	public String parseCommand(String x){
		String[] rest=new String[1];
		try{
		if(Util.parseKeyWord(x, "run", rest)){
			if(!this.running){
				this.running=true;
				this.runProgram(rProgram);
				this.running=false;
				return "OK";
			}
			else{
				return "ERROR already running.";
			}
		}
		else
		if(Util.parseKeyWord(x, "set ", rest)){
			  String xx=rest[0];
			   xx=Util.skipSpace(xx);
			   if(Util.parseKeyWord(xx, "logging=", rest)){
				     xx=rest[0];
				     xx=Util.skipSpace(xx);
				       if(Util.parseKeyWord(xx, "on", rest)){
				            logging=true;
				            return "OK";
				       }
				       else
				       if(Util.parseKeyWord(xx, "off", rest)){
				            logging=false;
				            return "OK";
				       }
			   }
			   return "ERROR";
		}
		else
		if(Util.parseKeyWord(x, "setInput ", rest)){
			this.rProgram=rest[0];
			return "OK";
		}
		else
		if(Util.parseKeyWord(x, "getOutput", rest)){
			// engine.end();
			return output;
		}
		else
		if(Util.parseKeyWord(x, "clearOutput", rest)){
			this.output="";
			return "OK";
		}
		else
		if(Util.parseKeyWord(x, "eval ", rest)){
			String line=rest[0];
			REXP result = engine.eval(line);
			System.out.println(result);
			if(logging){
			    output=output + result +"\n";
			}
			return result.asString();
		}
		else
		if(Util.parseKeyWord(x, "getVector ", rest)){
			String line=rest[0];
			line=Util.skipSpace(line);
			String[] breaks=new String[1];
			String breakSymbol=",";
			if(Util.parseStrConst(line, breaks, rest)){
				line=Util.skipSpace(rest[0]);
				breakSymbol=breaks[0];
		 }
			REXP result = engine.eval(line);
			System.out.println(result);
			if(logging){
			    output=output + result +"\n";
			}
			String[] xarray=result.asStringArray();
			String rtn=xarray[0];
			for(int i=1;i<xarray.length;i++){
				rtn=rtn+breakSymbol+xarray[i];
			}
			return rtn;			

		}
		return "ERROR";
		}
		catch(Exception e){
			System.out.println("Rprogram.parseCommand("+x+") error:"+e);
			return "ERROR";
		}		
	}
	public InterpreterInterface lookUp(String x){
		return null;
	}
}