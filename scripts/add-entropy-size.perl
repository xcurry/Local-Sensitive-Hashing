# -*- perl -*-

$ln2 = log(2.0);
$lastThreadId = 0;
$entropy = 0;

while (<STDIN>) {
  chomp;
  $line = $_;
  ($date, $threadId, $user, $text) = split(/\t/, $line);
  next if ($text eq "");
  if ($threadId > $lastThreadId) {
    computeEntropy();
    if ($entropy > 3.5) {
      printThread();
    }
    undef %users;
    undef @thread;
  }

  $text = tokenize($text);
  @toks = split(/\s+/, $text);
  foreach $tok (@toks) {
    ++$unigrams{$tok};
    ++$totalCount;
  }
  ++$users{$user};
  push(@thread, $line);
  $lastThreadId = $threadId;
}

if (scalar(@thread) > 0) {
  computeEntropy();
  printThread();
}

sub computeEntropy {
  $entropy = 0;
  foreach $tweet (@thread) {
    local ($date, $threadId, $user, $text) = split(/\t/, $tweet);
    $text = tokenize($text);
    @toks = split(/\s+/, $text);
    foreach $tok (@toks) {
      $p = $unigrams{$tok} / $totalCount;
      $entropy -= $p * log($p);
    }
  }
  $entropy /= $ln2;
  undef %unigrams;
  $totalCount = 0;
}

sub printThread {
  #$size = scalar(@thread);
  $uniqUsers = scalar(keys(%users));
  $i = 0;
  foreach $tweet (@thread) {
    last if (++$i > 5);
    #print $entropy."\t".$size."\t".$tweet."\n";
    print $entropy."\t".$uniqUsers."\t".$tweet."\n";
  }
}

sub tokenize {
  local $text = shift;
  $text = lc($text);
  $text =~ s:\<.*?\>::g;
  $text =~ s:\W: :g;
  $text =~ s:\s+: :g;

  $text =~ s:^\s+::;
  $text =~ s:\s+$::;

  return $text;
}

