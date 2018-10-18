/*
Dims.java
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
 * Written by Rauli Ruohonen
 */
package org.gzigzag.impl;
import org.gzigzag.*;

/** WHY IS THIS NOT DOCUMENTED!?!?!?! --TJL.
 */

public class Dims {

    private String primitiveSpace = 
	"0000000008000000E837B2C6B900041918A5E678E609131C9F1815EA03D011FC0FB4E34B615DF2";


    private static String cursorprefix =
	"home-id:0000000008000000E7A85A82DF000418634ECEBC414504987F02249EF1BF187064020941249F18-";
    private static String vstreamprefix =
	"home-id:0000000008000000E7D69C9A150004C3476FCCA36883FD4DB32E101A6AE76CA1D83853C771C211-";
    public static Cell d_clone_id =
	Id.space.getCell("home-id:0000000008000000E7A210622100040BD470AB849852D974C8CF69CAD2A3E6833B95298942F09E-1");
    public static Cell d_cells_id =
        Id.space.getCell("home-id:0000000008000000E7A210622100040BD470AB849852D974C8CF69CAD2A3E6833B95298942F09E-2");
    public static Cell d_dims_id =
        Id.space.getCell("home-id:0000000008000000E7A210622100040BD470AB849852D974C8CF69CAD2A3E6833B95298942F09E-3");
    public static Cell d_cursor_id = Id.space.getCell(cursorprefix+"1");
    public static Cell d_cursor_list_id = Id.space.getCell(cursorprefix+"2");
    public static Cell d_cursor_cargo_id = Id.space.getCell(cursorprefix+"3");
    public static Cell d_spaces_id =
	Id.space.getCell("home-id:0000000008000000E7ABBBCBEC0004A2A59677381A659A18A592B04488C92DA6799C228D79F330-1");
    public static Cell d_spacespec_id =
	Id.space.getCell("home-id:0000000008000000E7BD6F28880004C62132A8CCF343828039ECC856521E3F49A054FF08883711-1");
    public static Cell d_vstream_id = Id.space.getCell(vstreamprefix+"1");
    public static Cell d_markup_id = Id.space.getCell(vstreamprefix+"2");
    public static Cell d_markup_list_id = Id.space.getCell(vstreamprefix+"3");
    public static Cell d_vstream_view_id = Id.space.getCell(vstreamprefix+"4");
    public static Cell d_markup_list_view_id = Id.space.getCell(vstreamprefix+"5");
    public static Cell d_cursor_params_id = Id.space.getCell("home-id:0000000008000000E7DA790CDF00041CCEAE25A1210480A64EE4A2C51D0FB428FB51C3C7FBB9AF-1");

    public static Cell d_image_ref_id = Id.space.getCell(
	"home-id:0000000008000000E837B2C6B900041918A5E678E609131C9F1815EA03D011FC0FB4E34B615DF2-1");

    public static Cell d_zob_type_id = Id.space.getCell(
	"home-id:0000000008000000EB98FB4E690004EFDFE00AF714335A68BEAC84068D32FEBAB08BA96EB5C187-3");

    public static Cell d_link_id = Id.space.getCell("home-id:0000000008000000E843A1F20A0004C54A68338D7DC94DD0B7287F25034936DAAC0C6F5F764224-1");

    public static Cell d_user_1_id = Id.space.getCell("home-id:0000000008000000E7C2E550C700043E3EC208FC03C2F68A854DA58908F1DB16B43BC4ACD60637-30");

    public static Cell d_user_2_id = Id.space.getCell("home-id:0000000008000000E7C2E550C700043E3EC208FC03C2F68A854DA58908F1DB16B43BC4ACD60637-32");

    public static Cell d_user_3_id = Id.space.getCell("home-id:0000000008000000E7C2E550C700043E3EC208FC03C2F68A854DA58908F1DB16B43BC4ACD60637-31");

    public static Cell d_span_overlap1 = Id.space.getCell("home-id:0000000008000000E9356DAB400004C53F024A51126AAA0015553408B87D2119F6B68CBF892ECA-3");
    public static Cell d_span_overlap2 = Id.space.getCell("home-id:0000000008000000E9356DAB400004C53F024A51126AAA0015553408B87D2119F6B68CBF892ECA-4");

    public static Cell get(Space s, Cell id) {
	if (!id.id.startsWith("home-id:"))
	    throw new IllegalArgumentException("Not an id cell: "+id);
	if (s.exists(Id.stripHome(id.id)))
	    return s.getCell(Id.stripHome(id.id));
	Dim d = s.getDim(d_spaces_id);
	Cell x = s.getHomeCell().s(d);
	while (x != null) {
	    String cid = x.id + ":" + Id.stripHome(id.id);
	    if (s.exists(cid)) return s.getCell(cid);
	    x = x.s(d);
	}
	return null;
    }
    // NoFail version of get(): throw an error if null would be returned.
    public static Cell getNF(Space s, Cell id) {
	Cell c = get(s, id);
	if (c == null)
	    throw new ZZError("Cell with id '"+id+"' not found in space "+s);
	return c;
    }
    public static Cell d_clone(Space s) {
	return getNF(s, d_clone_id);
    }

/*
    static Space primitives = null;

    public String getDimName(Cell id) {
	id = Id.stripHome(id);
	if(primitives == null)
	    primitives = new PermanentSpace(
    }
*/
}
