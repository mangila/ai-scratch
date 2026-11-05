# {{entity}}

**Identifier: {{identifier}}**
**Entity: {{entity}}**

- Generation: {{species.generation.name}}
- Types:
    {{#pokemon.types}}
    - Type **{{type.name}}** (Slot: {{slot}})
    {{/pokemon.types}}
- Habitat: {{species.habitat.name}}
- Capture Rate: {{species.capture_rate}}
- Evolution Line:
    {{#evolutions}}
    - Evolution **{{name}}** (Index: {{index}})
    {{/evolutions}}
- Base Experience: {{pokemon.base_experience}}
- Stats:
    {{#pokemon.stats}}
    - Stat **{{stat.name}}**: {{base_stat}}
    {{/pokemon.stats}}
- Height: {{pokemon.height}} decimetres
- Weight: {{pokemon.weight}} hectograms
- Official Artwork: {{pokemon.sprites.other.official-artwork.front_default}}
- Default Sprite: {{pokemon.sprites.front_default}}

{{#species.flavor_text_entries}}
    {{#is_english}}
- (Game) {{version.name}}: {{flavor_text}}
    {{/is_english}}
{{/species.flavor_text_entries}}