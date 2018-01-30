/**
 * This file is part of CERMINE project.
 * Copyright (c) 2011-2018 ICM-UW
 *
 * CERMINE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CERMINE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with CERMINE. If not, see <http://www.gnu.org/licenses/>.
 */

package pl.edu.icm.cermine.content.headers.features;

import java.util.List;
import java.util.Map;
import pl.edu.icm.cermine.structure.model.BxLine;
import pl.edu.icm.cermine.structure.model.BxPage;
import pl.edu.icm.cermine.tools.classification.general.FeatureCalculator;

/**
 * @author Jan Lasek
 */
public class FontCodeFeature extends FeatureCalculator<BxLine, BxPage> {
    String[] fontNames;

    public FontCodeFeature(List<Map.Entry<String, Integer>> fontNames2) {
        String[] fontNamesTmp = new String[fontNames2.size()];
        for(int i = 0; i < fontNamesTmp.length; i++) {
            fontNamesTmp[i] = fontNames2.get(i).getKey();
        }
        fontNames = fontNamesTmp;
    }
       
    @Override
    public double calculateFeatureValue(BxLine object, BxPage context) {
        String fn = object.getMostPopularFontName();
        int i = 0;
        while(i < fontNames.length && !fn.equals(this.fontNames[i])) {
            i++;
        }
        return (double) i;
    }

}
