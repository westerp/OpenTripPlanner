/* This program is free software: you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public License
 as published by the Free Software Foundation, either version 3 of
 the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>. */

package org.opentripplanner.routing.edgetype.flex;

import com.vividsolutions.jts.linearref.LengthIndexedLine;
import org.onebusaway.gtfs.model.Stop;
import org.opentripplanner.common.geometry.SphericalDistanceLibrary;
import org.opentripplanner.routing.core.RoutingRequest;
import org.opentripplanner.routing.edgetype.PartialPatternHop;
import org.opentripplanner.routing.edgetype.PatternHop;
import org.opentripplanner.routing.edgetype.TemporaryEdge;
import org.opentripplanner.routing.vertextype.PatternArriveVertex;
import org.opentripplanner.routing.vertextype.PatternDepartVertex;
import org.opentripplanner.routing.vertextype.PatternStopVertex;

public class TemporaryPartialPatternHop extends PartialPatternHop implements TemporaryEdge {
    public TemporaryPartialPatternHop(PatternHop hop, PatternStopVertex from, PatternStopVertex to, Stop fromStop, Stop toStop, double startIndex, double endIndex, double buffer) {
        super(hop, from, to, fromStop, toStop, startIndex, endIndex, buffer);
    }

    // todo can this be smarter
    // start hop is a hop from the existing origin TO a new flag destination
    public static TemporaryPartialPatternHop startHop(RoutingRequest opt, PatternHop hop, PatternArriveVertex to, Stop toStop) {
        LengthIndexedLine line = new LengthIndexedLine(hop.getGeometry());
        return new TemporaryPartialPatternHop(hop, (PatternStopVertex) hop.getFromVertex(), to, hop.getBeginStop(), toStop, line.getStartIndex(), line.project(to.getCoordinate()), opt.flagStopBufferSize);
    }

    public static TemporaryPartialPatternHop endHop(RoutingRequest opt, PatternHop hop, PatternDepartVertex from, Stop fromStop) {
        LengthIndexedLine line = new LengthIndexedLine(hop.getGeometry());
        return new TemporaryPartialPatternHop(hop, from, (PatternStopVertex) hop.getToVertex(), fromStop, hop.getEndStop(), line.project(from.getCoordinate()), line.getEndIndex(), opt.flagStopBufferSize);
    }

    public TemporaryPartialPatternHop shortenEnd(RoutingRequest opt, PatternStopVertex to, Stop toStop) {
        LengthIndexedLine line = new LengthIndexedLine(getOriginalHop().getGeometry());
        double endIndex = line.project(to.getCoordinate());
        if (endIndex < getStartIndex())
            return null;
        return new TemporaryPartialPatternHop(getOriginalHop(), (PatternStopVertex) getFromVertex(), to, getBeginStop(), toStop, getStartIndex(), endIndex, opt.flagStopBufferSize);
    }

    @Override
    public void dispose() {
        fromv.removeOutgoing(this);
        tov.removeIncoming(this);
    }

    // is this hop too not-different to care about? for now lets say should be > 50 m shorter than original hop
    public boolean isTrivial() {
        double length = SphericalDistanceLibrary.fastLength(getGeometry());
        double parentLength = SphericalDistanceLibrary.fastLength(getOriginalHop().getGeometry());
        return length + 50 >= parentLength;
    }

}
