package name.szul.edu.kaggle.facebook

class TestFeatureProvider extends FeatureProvider[Long] {

  @Override
  val name = "test_feature"
  
  @Override
  def compute:Iterable[Tuple2[String,Long]] = List(
          ("dddddddd",100L),
          ("ddeeeeee",150)
      );
}