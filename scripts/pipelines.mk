
%.grouped: %.all_threads
	sort -k2,2 < $< > $@

%.ent_sz: %.grouped
	perl add-entropy-size.perl < $< | sort -k2,2gr -k1,1gr > $@

