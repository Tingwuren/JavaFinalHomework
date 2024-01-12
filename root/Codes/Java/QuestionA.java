/*
*问题 A: 简单a+b
*1.题目描述：输入两个整数a和b，计算a+b的和。
*2.输入：两个用空格分隔的整数，分别代表a和b（0<a,b<10000）。
*3.输出：a+b的和。
*4.样例输入：1 2
*5.样例输出：3
*/
import java.util.Scanner;
public class QuestionA{
	public static void main(String args[]){
		Scanner cin=new Scanner(System.in);
		int a,b;
		a=cin.nextInt();
		b=cin.nextInt();
		System.out.println(a+b);
		cin.close();
	}
}