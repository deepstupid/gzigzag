/*
Matrix.java
 *
 *    Copyright (c) 2001, Ted Nelson and Tuomas Lukka
 *
 *    You may use and distribute under the terms of either the GNU Lesser
 *    General Public License, either version 2 of the license or,
 *    at your choice, any later version. Alternatively, you may use and
 *    distribute under the terms of the XPL.
 *
 *    See the LICENSE.lgpl and LICENSE.xpl files for the specific terms of
 *    the licenses.
 *
 *    This software is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the README
 *    file for more details.
 *
 */
/*
 * Written by Tero Mäyränen
 */

package org.gzigzag.module.raytrace;
import java.awt.*;

/*
 *	"No-one can be told what The Matrix is... you have to see it for yourself"
 *	- Morpheus
 */

public class Matrix
{
	public static final String rcsid = "$Id: Matrix.java,v 1.1 2001/06/06 10:18:15 deetsay Exp $";

	protected int n, m;

	protected double[] data;

	public int getn() { return n; }

	public int getm() { return m; }

	public double[] getdata() { return data; }

	public Matrix(int mn, int mm)
	{
		n = mn;
		m = mm;
		data = new double[m * n];
		int ix = 0;
		for (int i=0; i<n; i++) for (int j=0; j<m; j++) data[ix++] = (i==j ? 1.0 : 0.0);
	}

	public Matrix(int mn, int mm, double[] mdata)
	{
		n = mn;
		m = mm;
		int size = n * m;
		data = new double[size];
		for (int i=0; i<size; i++) data[i] = mdata[i];
	}

	public Matrix(Matrix M) throws IndexOutOfBoundsException
	{
		n = M.getn();
		m = M.getm();
		int size = n * m;
		double mdata[] = M.getdata();
		data = new double[size];
		for (int i=0; i<size; i++) data[i] = mdata[i];
	}

	public void set(Matrix M) throws IndexOutOfBoundsException
	{
		if (n != M.getn() || m != M.getm()) throw new IndexOutOfBoundsException();
		double Mdata[] = M.getdata();
		for (int i=0; i<(m*n); i++) { data[i] = Mdata[i]; }
	}

	public void set(double Mdata[]) throws IndexOutOfBoundsException
	{
		for (int i=0; i<(m*n); i++) { data[i] = Mdata[i]; }
	}

/*	public static void transpose(Matrix M, Matrix R) throws IndexOutOfBoundsException
	{
		if ((M.getn() != R.getm()) || (M.getm() != R.getn())) throw new IndexOutOfBoundsException();

		double Mdata[] = M.getdata();
		double Rdata[] = R.getdata();
		int mn = M.getn();
		int mm = M.getm();
		for (int i=0; i<mn; i++) for (int j=0; j<mm; j++) Rdata[(i*mm)+j] = Mdata[(j*mn)+i];
	}
*/

//	Here's a couple of ways to invert a matrix, I think the last one might even work =)

/*
	// Gauss-Jordan with partial pivoting
	public static void invert(Matrix M1, Matrix M2) throws IndexOutOfBoundsException
	{
		int mn = M1.getn();
		M2.set(M1);
		double t = 0;
		double Mdata[] = M2.getdata();

		double S[] = new double[mn];
		int O[] = new int[mn];

		Matrix R = new Matrix(mn, mn);
		double Rdata[] = R.getdata();
		int ix = 0;
		for (int i=0; i<mn; i++) for (int j=0; j<mn; j++) Rdata[ix++] = (i==j ? 1.0 : 0.0);

		for (int i=0; i<mn; i++)
		{
			O[i] = i;
			S[i] = Math.abs(Mdata[i]*mn);
			for (int j=1; j<mn; j++)
			{
				t = Math.abs(Mdata[(i*mn)+j]);
				if (t > S[i]) S[i] = t;
			}
		}

		for (int k=0; k<mn; k++)
		{
			if (k != mn-1) pivoting(M2, S, O, k);
			t = Mdata[(O[k]*mn)+k];
			for (int j=0; j<mn; j++)
			{
				Mdata[(O[k]*mn)+j] /= t;
				Rdata[(O[k]*mn)+j] /= t;
			}
			for (int i=0; i<mn; i++)
			{
				if (i != k)
				{
					t = Mdata[(O[i]*mn)+k];
					for (int j=0; j<mn; j++)
					{
						Mdata[(O[i]*mn)+j] -= t*Mdata[(O[k]*mn)+j];
						Rdata[(O[i]*mn)+j] -= t*Rdata[(O[k]*mn)+j];
					}
				}
			}
		}
	}

	public static void pivoting(Matrix M, double S[], int O[], int k)
	{
		int mn = M.getn();

		int pivot = k;
		double t = 0;
		double Mdata[] = M.getdata();
		double big = Math.abs(Mdata[(O[k]*mn)+k] / S[O[k]]);

		for (int i=k+1; i<mn; i++)
		{
			t = Math.abs(Mdata[(O[i]*mn)+k] / S[O[i]]);
			if (t > big)
			{
				big = t;
				pivot = i;
			}
		}
		int tmp = O[pivot];
		O[pivot] = O[k];
		O[k] = tmp;
	}
*/
/*
	// Gauss-Jordan with full pivoting
	public static void invert(Matrix M1, Matrix M2) throws IndexOutOfBoundsException
	{
		M2.set(M1);
		int mn = M2.getn();
		double A[] = M2.getdata();
		double big, dum, piv, temp;

		int icol=0, irow=0;
		int indxc[] = new int[mn];
		int indxr[] = new int[mn];
		int ipiv[] = new int[mn];

		for (int j=0; j<mn; j++) ipiv[j] = 0;
		for (int i=0; i<mn; i++)
		{
			big = 0.0;
			for (int j=0; j<mn; j++)
			{
				if (ipiv[j] != 1)
				{
					for (int k=0; k<mn; k++)
					{
						if (ipiv[k] == 0)
						{
							temp = Math.abs(A[(j*mn)+k]);
							if (temp >= big)
							{
								big = temp;
								irow = j;
								icol = k;
							}
						}
						else if (ipiv[k] > 1) throw new IndexOutOfBoundsException();
					}
				}
			}
			ipiv[icol]++;

			if (irow != icol)
			{
				for (int l=0; l<mn; l++)
				{
					temp = A[(irow*mn)+l];
					A[(irow*mn)+l] = A[(icol*mn)+l];
					A[(icol*mn)+l] = temp;
				}
			}
			indxr[i] = irow;
			indxc[i] = icol;
//			if (A[(icol*mn)+icol] == 0.0) throw new IndexOutOfBoundsException();
//			pivinv = 1.0 / A[(icol*mn)+icol];
			piv = A[(icol*mn)+icol];
//			if (piv == 0.0) throw new IndexOutOfBoundsException();
			A[(icol*mn)+icol] = 1.0;
			for (int l=0; l<mn; l++) A[(icol*mn)+l] /= piv;

			for (int ll=0; ll<mn; ll++)
			{
				if (ll != icol)
				{
					dum = A[(ll*mn)+icol];
					A[(ll*mn)+icol] = 0.0;
					for (int l=0; l<mn; l++) A[(ll*mn)+l] -= dum * A[(icol*mn)+l];
				}
			}
		}
		for (int l=mn-1; l>=0; l--)
		{
			if (indxr[l] != indxc[l])
			{
				for (int k=0; k<mn; k++)
				{
					temp = A[(k*mn)+indxr[l]];
					A[(k*mn)+indxr[l]] = A[(k*mn)+indxc[l]];
					A[(k*mn)+indxc[l]] = temp;
				}
			}
		}
	}
*/
	public static void multiply(Matrix M2, Matrix M1, Matrix R) throws IndexOutOfBoundsException
	{
		if (M1.getn() != M2.getm()) throw new IndexOutOfBoundsException();
		int M1n = M1.getn();
		int M1m = M1.getm();
		int M2n = M2.getn();
		if ((R.getn() != M2n) || (R.getm() != M1m)) throw new IndexOutOfBoundsException();
//		Matrix R = new Matrix(M2n, M1m);
		double Rdata[] = R.getdata();

		int k=0;
		for (int i=0; i<M1m; i++)
		{
			for (int j=0; j<M2n; j++)
			{
				Rdata[k++] = 0.0;
			}
		}

		double M1data[] = M1.getdata();
		double M2data[] = M2.getdata();
		for (int i=0; i<M1n; i++)
		{
			for (int j=0; j<M1m; j++)
			{
				for (k=0; k<M2n; k++)
				{
					Rdata[(j*M2n)+k] += M1data[(j*M1n)+i] * M2data[(i*M2n)+k];
				}
			}
		}
	}
}
