//******************************************************************************
// interface oblivion.awt.colorbrewer.Palette1D
// Copyright (c) Christopher E. Weaver. All rights reserved.
//******************************************************************************
// File: Palette1D.java
// Last modified: Mon Nov 14 17:55:21 2005 by Chris Weaver
//******************************************************************************
// Modification History:
//
// 20050907 [weaver]: Original file.
//
//******************************************************************************
//
//******************************************************************************

package geovista.colorbrewer;

//import java.lang.*;
import java.awt.Color;

//******************************************************************************
// interface Palette1D
//******************************************************************************

/**
 * The <CODE>Palette1D</CODE> interface.
 * 
 * @author Chris Weaver
 * @version %I%, %G%
 */
public interface Palette1D extends Palette {
	// **********************************************************************
	// Members
	// **********************************************************************
	public enum SequenceType {
		SEQUENTIAL, DIVERGING, QUALITATIVE
	}

	// **********************************************************************
	// Methods
	// **********************************************************************

	SequenceType getType();

	Color[] getColors(int length);
}

// ******************************************************************************