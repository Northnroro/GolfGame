package game.debug;

public class StackTrace {
	public static void printStackTrace(){
		//------------------- trace which method call this method -----------------------//
		Throwable t = new Throwable(); 
		StackTraceElement[] elements = t.getStackTrace(); 
		for(int i=1;i<elements.length;i++){
			System.out.print("[" + i + ": " + elements[i].getClassName() + " : " + elements[i].getMethodName() + "] ");
		}
		System.out.println();
		//*******************************************************************************//
	}
}
