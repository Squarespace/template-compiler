package com.squarespace.template;

import static com.squarespace.template.ExecuteErrorType.APPLY_PARTIAL_MISSING;
import static com.squarespace.template.ExecuteErrorType.APPLY_PARTIAL_SYNTAX;
import static com.squarespace.template.Patterns.WHITESPACE;

import java.util.regex.Pattern;


/**
 * A formatter is a function that examines a value and emits a string. Formatters
 * can have zero or more arguments.
 */
class CoreFormatters {

  // Group the TBD formatters below into logical groups, like Image, Commerce, Social
  // Sort out which formatters are included with the JS upstream dist, and keep those
  // in Core, then make sure there is an initialization method for registering additional
  // formatter packs.
  
  // TBD: from json-template.js

  // pluralize (implement for real)
  // encode-space
  // truncate
  // date
  // image
  // timesince
  // resizedHeightForWidth
  // resizedWidthForHeight
  // squarespaceThumbnailForWidth
  // squarespaceThumbnailForHeight
  // cycle

  
  // TBD: from template-helpers.js
  
  // html
  // htmltag
  // htmlattr
  // str
  // safe
  // json-pretty
  // smartypants
  // url-encode
  // activate-twitter-links
  // count
  // audio-player
  // social-button
  // social-button-inline
  // twitter-follow-button
  // comments
  // comment-link
  // like-button
  // comment-count
  // image-meta
  // height
  // width
  // moneyFormat
  // money-format
  // google-calendar-url
  // AbsUrl
  // item-classes
  // round
  // iter
  // product-status
  // money-string
  // product-price
  // product-checkout
  // from-price
  // normal-price
  // sale-price
  // coupon-descriptor
  // variant-descriptor
  // variants-attributes
  // variants-select
  // quantity-input
  // pay-what-you-want-input
  // add-to-cart-btn
  // color-weight
  

  /**
   * APPLY - This will compile and execute a "partial template", caching it in the
   * context for possible later use.
   */
  public static Formatter APPLY = new BaseFormatter("apply", true) {
    
    @Override
    public void validateArgs(Arguments args) throws ArgumentsException {
      args.exactly(1);
    }
    
    @Override
    public void apply(Context ctx, Arguments args) throws CodeExecuteException {
      String name = args.first();
      Instruction inst = null;
      try {
        inst = ctx.getPartial(name);
      } catch (CodeSyntaxException e) {
        // The partial template failed to parse. We need to raise an error.
        throw new CodeExecuteException(ctx.error(APPLY_PARTIAL_SYNTAX).name(name).data(e.getMessage()));
      }
      
      if (inst == null) {
        throw new CodeExecuteException(ctx.error(APPLY_PARTIAL_MISSING).name(name));
      }
      
      ctx.getCompiler().execute(inst, ctx.node(), ctx.getBuffer());
    }
  };
  
  
  /**
   * JSON - Output a text representation of the node.
   */
  public static Formatter JSON = new BaseFormatter("json", false) {
    @Override
    public void apply(Context ctx, Arguments args) throws CodeExecuteException {
      ctx.append(ctx.node().toString());
    }
  };


  /**
   * PLURALIZE - Emit a string based on the plurality of the node.
   */
  public static Formatter PLURALIZE = new BaseFormatter("pluralize", false) {
    
    @Override
    public void validateArgs(Arguments args) throws ArgumentsException {
      args.between(0, 2);
    }
    
    @Override
    public void apply(Context ctx, Arguments args) throws CodeExecuteException {
      // defaults for args.size() == 0
      String singular = "";
      String plural = "s";
      if (args.count() == 1) {
        plural = args.get(0);
      } else if (args.count() == 2) {
        singular = args.get(0);
        plural = args.get(1);
      }
      CharSequence result = (ctx.node().asLong() > 1) ? plural : singular;
      ctx.append(result);
    }
  };

  /**
   * SLUGIFY - Turn headline text into a slug.
   */
  public static Formatter SLUGIFY = new BaseFormatter("slugify", false) {
    
    private final Pattern SLUG_KILLCHARS = Pattern.compile("[^a-zA-Z0-9\\s-]+");
    
    @Override
    public void apply(Context ctx, Arguments args) throws CodeExecuteException {
      String result = ctx.node().asText();
      result = SLUG_KILLCHARS.matcher(result).replaceAll("");
      result = WHITESPACE.matcher(result).replaceAll("-");
      ctx.append(result.toLowerCase());
    }
  };
  
}
