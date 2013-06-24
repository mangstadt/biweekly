package biweekly.component.marshaller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import biweekly.component.VFreeBusy;
import biweekly.property.FreeBusy;
import biweekly.property.ICalProperty;
import biweekly.util.Period;

/*
 Copyright (c) 2013, Michael Angstadt
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met: 

 1. Redistributions of source code must retain the above copyright notice, this
 list of conditions and the following disclaimer. 
 2. Redistributions in binary form must reproduce the above copyright notice,
 this list of conditions and the following disclaimer in the documentation
 and/or other materials provided with the distribution. 

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/**
 * @author Michael Angstadt
 */
public class VFreeBusyMarshaller extends ICalComponentMarshaller<VFreeBusy> {
	public VFreeBusyMarshaller() {
		super(VFreeBusy.class, "VFREEBUSY");
	}

	@Override
	public Collection<ICalProperty> getProperties(VFreeBusy component) {
		//sort FREEBUSY properties by start date
		//TODO this will require that a List be returned
		List<FreeBusy> fb = new ArrayList<FreeBusy>(component.getFreeBusy());
		Collections.sort(fb, new Comparator<FreeBusy>() {
			public int compare(FreeBusy one, FreeBusy two) {
				Date oneStart = getEarliestStartDate(one);
				Date twoStart = getEarliestStartDate(two);
				if (oneStart == null && twoStart == null) {
					return 0;
				}
				if (oneStart == null) {
					return 1;
				}
				if (twoStart == null) {
					return -1;
				}
				return oneStart.compareTo(twoStart);
			}

			private Date getEarliestStartDate(FreeBusy fb) {
				Date date = null;
				for (Period tp : fb.getValues()) {
					if (tp.getStartDate() == null) {
						continue;
					}
					if (date == null || date.compareTo(tp.getStartDate()) > 0) {
						date = tp.getStartDate();
					}
				}
				return date;
			}
		});

		//make sure the FREEBUSY properties appear in sorted order
		Collection<ICalProperty> all = super.getProperties(component);
		for (FreeBusy f : fb) {
			if (all.remove(f)) {
				all.add(f);
			}
		}

		return all;
	}

	@Override
	public VFreeBusy newInstance() {
		return new VFreeBusy();
	}
}
