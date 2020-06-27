# Burninator

Make your own magma blocks, but Fabric. This time, it uses block tags instead of a config file. Get out your datapacks!

Magma block rules apply (it deals 1 ping of hot-floor damage, Frost Walker saves you, fire-immune entities don't count)

## Block filtering

These tags go in the `blocks` folder.

* Blocks tagged with `%s` will damage `%s` entities that are standing on them:
    * `burninator:very_hot`, all
    * `burninator:hot`, not sneaking (like magma blocks)
    * `burninator:reverse_hot`, sneaking

(To clarify, `burninator:hot` refers to the file `datapacks/<pack_name>/data/burninator/tags/blocks/hot.json`)

## Entity filtering

These tags go in the `entity_types` folder.

* Entities in `burninator:ignored_entities` will be *excluded* from taking damage in this way.
* If it is not empty, entities *not* in the `burninator:only_entities` tag will also be excluded.
    * If `burninator:only_entities` is not defined or overridden to have zero values, it will do nothing.

### License bullshit ugh

LGPL v3 or later