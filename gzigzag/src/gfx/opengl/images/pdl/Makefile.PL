use PDL::Core::Dev;            # Pick up development utilities
use ExtUtils::MakeMaker;

$package = ["pdlperlin.pd",Perlin,PDL::Perlin];
%hash = pdlpp_stdargs($package);
$hash{'VERSION_FROM'} = 'pdlperlin.pd';
WriteMakefile(%hash);

sub MY::postamble { pdlpp_postamble($package); }


