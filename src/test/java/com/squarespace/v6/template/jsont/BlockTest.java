package com.squarespace.template;

import org.testng.Assert;
import org.testng.annotations.Test;


@Test( groups={ "unit" })
public class BlockTest extends UnitTestBase {

  @Test
  public void testEquality() {
    CodeMaker maker = maker();
    Block b1 = new Block(2);
    b1.add(maker.text("foo"), maker.end(), maker.eof());
    Block b2 = new Block(2);
    b2.add(maker.text("foo"), maker.end(), maker.eof());
    Assert.assertEquals(b1, b2);
    
    Block b3 = new Block(2);
    b3.add(maker.text("foo"), maker.eof(), maker.end());
    Assert.assertNotEquals(b1, b3);
    
    Block b4 = new Block(2);
    b4.add(maker.text("foo"));
    Assert.assertNotEquals(b1, b4);
    Assert.assertNotEquals(b3, b4);

    Assert.assertFalse(b1.equals(null));
    Assert.assertFalse(b1.equals("foo"));
  }
  
  
}
