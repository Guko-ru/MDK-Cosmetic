from pathlib import Path
import json, re, shutil, itertools, collections, math
ROOT=Path(__file__).resolve().parent.parent
ADV=ROOT/'src/main/resources/data/mdkcosmetic/advancement'
ADV.mkdir(parents=True,exist_ok=True)
LANG=ROOT/'src/main/resources/assets/mdkcosmetic/lang'
METADATA={}
TITLES={}
COUNT=0
COLORS={
 'common':'#81E6A0',
 'uncommon':'#71D5FF',
 'rare':'#C79CFF',
 'epic':'#FFC86C',
 'legendary':'#FF6A83'
}
FRAMES={'common':'task','uncommon':'goal','rare':'goal','epic':'challenge','legendary':'challenge'}

def ident(v): return 'mdkcosmetic:'+v

def item(item,amount=1):
    return {'trigger':'minecraft:inventory_changed','conditions':{'items':[{'items':[item], 'count':{'min':amount}}]}}

def consume(itemid):
    return {'trigger':'minecraft:consume_item','conditions':{'item':{'items':[itemid]}}}

def kill(etype):
    return {'trigger':'minecraft:player_killed_entity','conditions':{'entity':{'type':etype}}}

def crafted(rid):
    return {'trigger':'minecraft:recipe_crafted','conditions':{'recipe_id':rid}}

def impossible(): return {'trigger':'minecraft:impossible'}

def put(path, title, description, icon, parent=None, rarity='common', crit=None, hidden=False, xp=None, progress=None):
    global COUNT
    assert path not in METADATA, path
    if crit is None: crit={'done':impossible()}
    if progress is not None:
        # 5 progress criteria at 20/40/60/80/100% threshold, each awarded by the server.
        crit={f'step_{i}':impossible() for i in range(1,6)}
    d={'display':{'icon':{'id':icon},
                  'title':{'translate':'advancements.mdkcosmetic.'+path+'.title','color':COLORS[rarity],'bold':rarity in ('epic','legendary')},
                  'description':{'translate':'advancements.mdkcosmetic.'+path+'.description','color':'gray'},
                  'frame':FRAMES[rarity], 'show_toast':True,
                  'announce_to_chat':rarity in ('epic','legendary'), 'hidden':bool(hidden)},
       'criteria':crit}
    if parent: d['parent']=ident(parent)
    if not parent: d['display']['background']='minecraft:textures/gui/advancements/backgrounds/stone.png'
    if xp is not None: d['rewards']={'experience':xp}
    if progress is not None: d['requirements']=[[f'step_{i}'] for i in range(1,6)]
    pathout=ADV/(path+'.json')
    pathout.parent.mkdir(parents=True,exist_ok=True)
    pathout.write_text(json.dumps(d,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    TITLES['advancements.mdkcosmetic.'+path+'.title']=(title, title)
    TITLES['advancements.mdkcosmetic.'+path+'.description']=(description,description)
    METADATA[path]={'name':title,'desc':description,'rarity':rarity,'type':('progress' if progress is not None else next(iter(crit.values()))['trigger']),'parent':parent,'progress':progress}
    COUNT+=1

# root and branches
put('root','Коллекция тайн','Путь от первой пушинки до тайны, скрытой за драконом.','mdkcosmetic:power_essence',rarity='uncommon',crit={'start':{'trigger':'minecraft:tick'}})
branches=[
 ('journey','Начало охоты','Научись собирать материалы и создавать предметы.','mdkcosmetic:tailoring_table'),
 ('wardrobe','Галерея образов','Каждый зверь оставляет свой след — собери всю коллекцию.','mdkcosmetic:fox_hat'),
 ('resources','Сокровища скалка','Найди осколки, монеты и богатства древних мест.','mdkcosmetic:power_essence'),
 ('combat','Кровавые хроники','Считай победы и покори опаснейших противников.','mdkcosmetic:collector_sword'),
 ('collector','Мастерская коллекционера','От первого стола к большому хранилищу памяти.','mdkcosmetic:collector_link'),
 ('mysteries','Запретные искусства','Сферы, зачарования, ярость и риск.','mdkcosmetic:greed_orb'),
 ('legend','Легенда без лица','Uno, Хранитель и испытания, достойные легенд.','mdkcosmetic:collector_memory'),
]
for p,t,desc,ic in branches:
    put(p,t,desc,ic,parent='root',rarity='uncommon',crit={'open':item(ic)})

# materials/furs and green cosmetics
animals=[
 ('fox','Лиса','fox_fur','fox_hat','fox'),('panda','Панда','panda_fur','panda_hat','panda'),
 ('cat','Кошка','cat_fur','cat_ears','cat'),('allay','Тихоня','allay_fibers','allay_hat','allay'),
 ('axolotl','Аксолотль','axolotl_shed','axolotl_hat','axolotl'),('rabbit','Кролик','rabbit_fur','rabbit_ears','rabbit'),
 ('squid','Спрут','squid_ink_clot','octopus_hat','squid'),('glow_squid','Светящийся спрут','glow_squid_ink_clot','glow_squid_hat','glow_squid'),
 ('camel','Верблюд','camel_fur','camel_hat','camel'),('cow','Корова','cow_fur','cow_hat','cow'),
 ('mooshroom','Грибная корова','mooshroom_fur','mooshroom_hat','mooshroom'),('polar_bear','Белый медведь','polar_bear_fur','polar_bear_hat','polar_bear'),
 ('llama','Лама','llama_fur','llama_hat','llama'),('sheep','Овца','sheep_fleece','sheep_hat','sheep'),
 ('wolf','Волк','wolf_fur','wolf_hat','wolf'),('horse','Лошадь','horse_fur','horse_hat','horse'),
 ('donkey','Осёл','donkey_fur','donkey_hat','donkey')]
for slug,an,mat,hat,fur_base in animals:
    ic=ident(mat); ih=ident(hat)
    put('fur/'+slug,'След '+an.lower(),'Добудь материал: '+an.lower()+'.',ic,parent='journey',crit={'collect':item(ic)})
    put('hat/'+slug,'Образ: '+an,'Получить декоративный головной убор — '+an.lower()+'.',ih,parent='wardrobe',rarity='common',crit={'collect':item(ih)})

put('wardrobe/full_green','Зелёная коллекция','Собери все 17 зелёных головных уборов одновременно — в инвентаре и/или памяти.','mdkcosmetic:fox_hat',parent='wardrobe',rarity='epic',hidden=False,xp=180)
put('wardrobe/all_furs','Вся шерсть мира','Собери все 17 видов материалов животных.','mdkcosmetic:fox_fur',parent='journey',rarity='rare',xp=80)
put('wardrobe/half_hats','Модник','Собери не менее 9 разных зелёных головных уборов.','mdkcosmetic:cat_ears',parent='wardrobe',rarity='uncommon')
put('wardrobe/one_worn','Не просто сувенир','Надень любую зелёную шапочку.','mdkcosmetic:fox_hat',parent='wardrobe')
put('wardrobe/sakura','Цветение мысли','Получить Сакуру мыслителя у странствующего торговца.','mdkcosmetic:sakura_thinker',parent='wardrobe',rarity='rare',crit={'collect':item('mdkcosmetic:sakura_thinker')})
put('wardrobe/mystic_gaze','Взгляд за грань','Получить Взор таинств.','mdkcosmetic:mystic_gaze',parent='wardrobe',rarity='rare',crit={'collect':item('mdkcosmetic:mystic_gaze')})

for slug,an,mat,hat,fur_base in animals:
    if slug=='sheep': continue
    idc=f'mdkcosmetic:{fur_base}_fur_carpet'
    # Verify carpet registry ID exists, skip otherwise
    if (ROOT/'src/main/resources/assets/mdkcosmetic/models/item'/f'{fur_base}_fur_carpet.json').exists():
        put('carpet/'+slug,'Мягкий шаг: '+an,'Получить ковёр из меха '+an.lower()+'.',idc,parent='journey',crit={'collect':item(idc)})

# colored mystery blocks actual IDs
mystery_models=list((ROOT/'src/main/resources/assets/mdkcosmetic/models/item').glob('*mystery*.json'))
# Filter known IDs from registered items in MysteryBlocks.java
mb=(ROOT/'src/main/java/com/mdk/cosmetic/MysteryBlocks.java').read_text(encoding='utf-8')
ids=sorted(set(re.findall(r'"([a-z0-9_]+)"\s*,\s*\(\)',mb)))
# We'll use explicit path lookup from resources to avoid fake IDs
for c in ['white','orange','magenta','light_blue','yellow','lime','pink','gray','light_gray','cyan','purple','blue','brown','green','red','black']:
    choices=[p.stem for p in (ROOT/'src/main/resources/assets/mdkcosmetic/models/item').glob('*.json') if 'mystery' in p.stem and c in p.stem]
    choices=[x for x in choices if x in mb]
    if choices:
        name=choices[0]
        put('color/'+c,'Тайна цвета: '+c.replace('_',' ').capitalize(),'Получить Блок таинств нужного цвета.',ident(name),parent='journey',crit={'collect':item(ident(name))})

# Main progression important tools and blocks
items=[
 ('first_shard','Первый осколок','Ты нашёл первый Осколок коллекции тайн.','power_essence','resources','common'),
 ('first_coin','Чеканщик тайн','Получить Монету коллекции тайн.','charged_power_essence','resources','uncommon'),
 ('tailor','Мастер нитей','Создай Стол для шитья.','tailoring_table','journey','common'),
 ('worktable','Работа тонкая','Получить Стол коллекционера.','essence_worktable','collector','uncommon'),
 ('casing','Тёмный каркас','Получить Коллекционный корпус.','collector_casing','collector','uncommon'),
 ('soul_storage','Заточённая душа','Получить Хранилище души коллекционера.','collector_soul_storage','collector','rare'),
 ('liaison','Посредник','Получить Связного коллекционера.','collector_link','collector','rare'),
 ('collector_soul','Тьма в бутылке','Получить предмет «Душа коллекционера».','collector_soul','resources','rare'),
 ('memory','Последняя мысль','Получить Память коллекционера после победы над Uno.','collector_memory','collector','epic'),
 ('memory_casing','За пределами куба','Получить Корпус памяти.','collector_memory_casing','collector','rare'),
 ('memory_core','Не забывай меня','Получить Ядро памяти коллекционера.','collector_memory_core','collector','epic'),
 ('collector_helmet','Чёрный лик','Получить Шлем коллекционера.','collector_helmet','wardrobe','rare'),
 ('collector_chestplate','Броня духа','Получить Нагрудник коллекционера.','collector_chestplate','wardrobe','rare'),
 ('collector_leggings','Поступь тайны','Получить Поножи коллекционера.','collector_leggings','wardrobe','rare'),
 ('collector_boots','Ни шагу назад','Получить Ботинки коллекционера.','collector_boots','wardrobe','rare'),
 ('collector_sword','Клинок без прощения','Получить Меч коллекционера.','collector_sword','combat','rare'),
 ('collector_pickaxe','Кирка древних','Получить Кирку коллекционера.','collector_pickaxe','resources','rare'),
 ('greed_orb','Сделка с жадностью','Получить Сферу Жадности.','greed_orb','mysteries','rare'),
 ('productivity_orb','Продуктивность любой ценой','Получить Сферу Продуктивности.','productivity_orb','mysteries','rare'),
]
for p,t,desc,ii,branch,rar in items:
    put('item/'+p,t,desc,ident(ii),parent=branch,rarity=rar,crit={'collect':item(ident(ii))},xp=15 if rar=='rare' else 5)

# ore types (use item and rare)
for slug,word in [('stone','Каменная'),('deepslate','Глубинная'),('netherrack','Адская'),('end_stone','Эндерская')]:
    id='collector_ore_'+slug
    put('ore/'+slug,word+' находка','Получить '+word.lower()+' руду коллекционера.',ident(id),parent='resources',rarity='uncommon',crit={'ore':item(ident(id))})

# consumables and travel
for name,ii,rar in [('greed','greed_orb','rare'),('productivity','productivity_orb','rare')]:
    put('orb/'+name+'_used',('Цена алчности' if name=='greed' else 'Без перерыва'),
        ('Использовать Сферу Жадности.' if name=='greed' else 'Использовать Сферу Продуктивности.'),
        ident(ii),parent='mysteries',rarity=rar,crit={'consume':consume(ident(ii))})
for dim,tt,ic in [('overworld','Шёпот поверхности','power_essence'),('the_nether','Цена жара','collector_ore_netherrack'),('the_end','Пустота смотрит в ответ','collector_ore_end_stone')]:
    put('travel/'+dim,tt,'Посетить измерение и продолжить поиски коллекции.',ident(ic),parent='resources',crit={'dimension':{'trigger':'minecraft:location','conditions':{'player':{'location':{'dimension':'minecraft:'+dim}}}}})

# Bosses, mob kills. Real vanilla condition 1.21
bosses=[('warden','Хранитель','Низвергнуть Хранителя.','minecraft:echo_shard','legendary'),
 ('ender_dragon','Дракон повержен','Убить Эндер-дракона.','minecraft:dragon_head','epic'),
 ('wither','Сломанная звезда','Убить Иссушителя.','minecraft:nether_star','epic'),
 ('elder_guardian','Страж глубин','Убить Древнего стража.','minecraft:prismarine_shard','rare'),
 ('evoker','Крах иллюзий','Убить Вызывателя.','minecraft:totem_of_undying','rare'),
 ('ravager','Сломанный таран','Убить Разорителя.','minecraft:saddle','rare'),
 ('piglin_brute','Кабанья ярость','Убить Пиглина-бестию.','minecraft:golden_axe','uncommon'),
 ('breeze','Сила ветра','Убить Вихря.','minecraft:breeze_rod','uncommon'),
 ('shulker','Коробка с сюрпризом','Убить Шалкера.','minecraft:shulker_shell','uncommon'),
 ('enderman','Схватка с Пустотой','Убить Эндермена.','minecraft:ender_pearl','common'),
 ('witch','Неудачная алхимия','Убить Ведьму.','minecraft:glowstone_dust','uncommon'),
 ('skeleton','Последний звук тетивы','Убить Скелета.','minecraft:bone','common'),
 ('creeper','Без взрыва','Убить Крипера.','minecraft:gunpowder','common'),
 ('zombie','Знакомая встреча','Убить Зомби.','minecraft:rotten_flesh','common'),
 ('spider','Паутина ошибок','Убить Паука.','minecraft:string','common'),
 ('blaze','Угольная ярость','Убить Ифрита.','minecraft:blaze_rod','uncommon'),
 ('ghast','Крик в воздухе','Убить Гаста.','minecraft:ghast_tear','uncommon'),
 ('guardian','Сквозь шипы','Убить Стража.','minecraft:prismarine_crystals','uncommon'),
 ('husk','Песок во рту','Убить Кадавра.','minecraft:sand','common'),
 ('silverfish','Боль древних камней','Убить Чешуйницу.','minecraft:stone','common'),
 ('slime','Слизкая работа','Убить Слизня.','minecraft:slime_ball','common'),
 ('phantom','Ночной дозор','Убить Фантома.','minecraft:phantom_membrane','uncommon')]
for mob,tt,descr,icon,rar in bosses:
    put('kills/'+mob,tt,descr,icon,parent='combat',rarity=rar,crit={'slain':kill('minecraft:'+mob)},xp={'common':5,'uncommon':10,'rare':30,'epic':100,'legendary':250}[rar])
for ent,title,rar in [('collector_soul_entity','Молчание под скалком','epic'),('uno','Конец безликого','legendary')]:
    put('kills/'+ent,title,'Победи '+('Душу коллекционера.' if ent=='collector_soul_entity' else 'Uno в честном бою.'),
        'mdkcosmetic:collector_soul' if ent=='collector_soul_entity' else 'mdkcosmetic:collector_memory',
        parent='legend',rarity=rar,crit={'slain':kill('mdkcosmetic:'+ent)},xp=150 if rar=='epic' else 350)

# Threshold milestones counted by server with 5 criteria each
PROGRESS={
 'kills_all':([10,50,100,500,1500,5000,10000], 'combat', 'minecraft:iron_sword','Убийца','Победи за всё время ', ' врагов.'),
 'kills_monsters':([25,100,500,1000], 'combat','minecraft:netherite_sword','Охотник на чудовищ','Победи ', ' враждебных мобов.'),
 'shard_pickups':([8,32,64,256,512,1024,4096,10000], 'resources', 'mdkcosmetic:power_essence', 'Осколочная лихорадка','Подними за всё время ', ' Осколков коллекции тайн.'),
 'coin_pickups':([1,8,32,64,256,1024], 'resources', 'mdkcosmetic:charged_power_essence','Монетный двор','Подними за всё время ', ' Монет коллекции тайн.'),
 'shards_held':([16,64,128,256,512], 'resources', 'mdkcosmetic:power_essence','Запасливый','Держи в инвентаре одновременно ', ' осколков.'),
 'trades':([1,5,25,100,500], 'collector', 'mdkcosmetic:collector_link','Железная сделка','Совершить ', ' покупок у Связного коллекционера.'),
 'memory_types':([1,10,32,64,128,256], 'collector', 'mdkcosmetic:collector_memory_core','Архивариус','Храни одновременно ', ' различных типов предметов в Памяти.'),
 'memory_stacks':([64,256,1024,4096,16384], 'collector','mdkcosmetic:collector_memory_core','Бездонная кладовая','Помести ', ' предметов в Память одновременно.'),
 'crafted_hats':([1,5,10,17,32], 'wardrobe','mdkcosmetic:fox_hat','Швейный мастер','Создай за всё время ', ' зелёных шапочек.'),
 'crafted_casings':([1,8,16,32,52], 'collector','mdkcosmetic:collector_memory_casing','Корпусных дел мастер','Создай ', ' корпусов памяти.'),
}
for metric,(thresholds,branch,icon,title_pre,pre,suf) in PROGRESS.items():
    last=branch
    for number in thresholds:
        slug=metric+'_'+str(number)
        rarity='common' if number==thresholds[0] else 'uncommon' if number in thresholds[1:3] else 'rare' if number in thresholds[3:5] else 'epic'
        if number>=4096 or (metric=='kills_all' and number>=10000):rarity='legendary'
        title=title_pre+' — '+format(number,',').replace(',',' ')
        put('milestone/'+slug,title,pre+str(number)+suf,icon,parent=last,rarity=rarity,xp=None,progress={'metric':metric,'target':number})
        last='milestone/'+slug

# Non-collectible custom challenge goals awarded by server side checks
CUSTOM=[
 ('complete_armor','Броня защищает тайну','Надень полный комплект брони коллекционера.','mdkcosmetic:collector_chestplate','wardrobe','epic',False),
 ('armor_carry','Хозяин реликвий','Имей все 4 части коллекционной брони и два инструмента.','mdkcosmetic:collector_sword','wardrobe','epic',False),
 ('wear_sakura','Цветение под небом','Носи Сакуру мыслителя.','mdkcosmetic:sakura_thinker','wardrobe','uncommon',False),
 ('wear_gaze','Смотрящий из огня','Носи Взор таинств.','mdkcosmetic:mystic_gaze','wardrobe','uncommon',False),
 ('fearless_warden','Не боюсь Хранителя','Окажись рядом с Хранителем в полном комплекте брони коллекционера.','mdkcosmetic:collector_helmet','legend','epic',True),
 ('assemble_3','Говорит связной','Собери работающий корпус коллекционера 3×3×3.','mdkcosmetic:collector_link','collector','rare',False),
 ('assemble_5','Память пробуждена','Дополни конструкцию до рабочего каркаса 5×5×5.','mdkcosmetic:collector_memory_core','collector','legendary',False),
 ('stress_half','Нервное хранилище','Добейся хотя бы 50% стресса у собранного хранилища.','mdkcosmetic:collector_soul_storage','mysteries','uncommon',False),
 ('stress_ninety','Шаг до катастрофы','Увидь стресс 90% или выше, пока конструкция ещё цела.','mdkcosmetic:collector_soul_storage','mysteries','epic',True),
 ('greedy','Вкус наживы','Подвергнись воздействию Сферы Жадности.','mdkcosmetic:greed_orb','mysteries','uncommon',False),
 ('productive','Не трать секунду','Подвергнись воздействию Сферы Продуктивности.','mdkcosmetic:productivity_orb','mysteries','uncommon',False),
 ('two_spheres','Ненасытный','Держи на себе эффекты обеих сфер одновременно.','mdkcosmetic:greed_orb','mysteries','epic',True),
 ('one_vs_uno','Рыцарь без доспехов','Убей Uno без надетой брони.','mdkcosmetic:collector_memory','legend','legendary',True),
 ('uno_sword','Суд клинка','Убей Uno, держа Меч коллекционера.','mdkcosmetic:collector_sword','legend','epic',True),
 ('end_uno','За финальным титром','Одолей Uno в измерении Энда.','minecraft:end_crystal','legend','legendary',False),
 ('soul_and_warden','Двое в темноте','Одолей Душу коллекционера, когда рядом существует Хранитель.','minecraft:echo_shard','legend','legendary',True),
 ('ultimate_collection','Наследие коллекционера','Собери все зелёные шапочки, полный комплект брони и закончи хранилище памяти.','mdkcosmetic:collector_memory','legend','legendary',True)
]
for slug,title,descr,ic,par,rar,hidden in CUSTOM:
    put('challenge/'+slug,title,descr,ic,parent=par,rarity=rar,hidden=hidden,xp=200 if rar=='legendary' else (70 if rar=='epic' else 10))

# Set progress names in manifest
manifest=ROOT/'docs/MDK_ADVANCEMENTS_CATALOG_RU.md'
manifest.parent.mkdir(parents=True,exist_ok=True)
by_type=collections.Counter()
for p,meta in METADATA.items():by_type[meta['rarity']]+=1
manifest.write_text('# MDK Cosmetic — достижения v1.18.50\n\n'+
    f'Всего **{COUNT} достижений** в одной вкладке.\n\n'+
    'Цвета: зелёный (обычные), голубой (необычные), фиолетовый (редкие), золотой (эпические), красный (легендарные).\n\n'+
    'Основные события реализованы через ванильные критерии. Счётчики сохраняются в SavedData мира, а прогресс пороговых испытаний отображается через пять критериев.\n\n'+
    '## Полный список\n\n'+'| ID | Название | Редкость |\n|---|---|---|\n'+
    '\n'.join(f'| `{k}` | {v["name"]} | {v["rarity"]} |' for k,v in METADATA.items())+'\n',encoding='utf-8')

# Save metadata for code generator and structural audits
(ROOT/'tools').mkdir(exist_ok=True)
(ROOT/'tools/advancements_manifest.json').write_text(json.dumps(METADATA,ensure_ascii=False,indent=2),encoding='utf-8')

# RU and EN - names in English fallback to transliterated path? generate idiomatic English for key entries
english={
 'root':('Collection of Mysteries','From your first fragment to the mystery beyond the dragon.'),
 'journey':('The First Step','Gather materials and make your first creations.'),
 'wardrobe':('Gallery of Faces','Collect every green-rarity animal hat.'),
 'resources':('Secrets of Sculk','Discover fragments, coins and the ore of mysteries.'),
 'combat':('The Hunter’s Chronicle','Track your victories and defeat dangerous enemies.'),
 'collector':('The Collector’s Workshop','Upgrade from a worktable to a limitless memory vault.'),
 'mysteries':('Forbidden Arts','Orbs, enchantments and dangerous stress.'),
 'legend':('The Faceless Legend','Face Uno, the Warden and deadly hidden challenges.'),
 'wardrobe/full_green':('Every Green Hat','Hold all 17 animal hats in inventory and/or connected memory.'),
 'wardrobe/all_furs':('The Whole Menagerie','Collect all 17 animal fur and fiber types.'),
 'wardrobe/half_hats':('Hat Enthusiast','Own at least 9 different green-rarity hats.'),
 'wardrobe/one_worn':('Wear It Proudly','Equip any green-rarity animal hat.'),
 'challenge/assemble_3':('The Liaison Speaks','Complete and activate the 3×3×3 Collector structure.'),
 'challenge/assemble_5':('Memory Awakens','Complete the advanced 5×5×5 Collector structure.'),
 'challenge/ultimate_collection':('Legacy of the Collector','Own all green hats, equip full Collector armor and activate Memory.'),
 'challenge/one_vs_uno':('Unarmored Duel','Defeat Uno without wearing any armor.'),
 'challenge/uno_sword':('Judgment of the Blade','Defeat Uno while holding the Collector Sword.'),
 'challenge/end_uno':('Beyond the Credits','Defeat Uno in the End.'),
 'challenge/soul_and_warden':('Two in the Darkness','Defeat the Collector Soul while a Warden is nearby.'),
 'challenge/complete_armor':('Armor of Mystery','Equip all four Collector armor pieces.'),
 'challenge/armor_carry':('Relic Keeper','Own the four armor pieces, sword and pickaxe.'),
 'challenge/greedy':('A Taste of Greed','Gain the Collector Greed effect.'),
 'challenge/productive':('Every Second Counts','Gain the Collector Productivity effect.'),
 'challenge/two_spheres':('Insatiable','Have both Sphere effects active at once.'),
 'challenge/stress_half':('Nervous Vault','See 50% or greater stress in an assembled Collector structure.'),
 'challenge/stress_ninety':('On the Brink','See at least 90% stress while the structure is still intact.'),
}
for slug,an,mat,hat,_ in animals:
    english['fur/'+slug]=(f'Fur: {an}',f'Obtain {mat.replace("_"," ")}.')
    english['hat/'+slug]=(f'Hat: {an}',f'Obtain the {hat.replace("_"," ")}.')
for metric,(thresholds,branch,icon,title,pre,suf) in PROGRESS.items():
    for n in thresholds:
        english['milestone/'+metric+'_'+str(n)]=(f'{metric.replace("_"," ").title()} — {n:,}',f'Complete the {metric.replace("_"," ")} milestone: {n:,}.')
for p,t,desc,ii,branch,rar in items:
    english['item/'+p]=(t, 'Obtain the '+ii.replace('_',' ')+'.')
for slug,tt,rar in [('stone','Stone','common'),('deepslate','Deepslate','common'),('netherrack','Nether','common'),('end_stone','End','common')]:
    english['ore/'+slug]=(tt+' Ore','Acquire the Collector ore block.')
for name in ['greed','productivity']:
    english['orb/'+name+'_used']=(name.title()+' Unleashed','Consume the Collector '+name+' sphere.')
for mob,tt,desc,ic,rar in bosses:
    english['kills/'+mob]=('Slay: '+mob.replace('_',' ').title(),'Defeat a '+mob.replace('_',' ')+'.')
english['kills/collector_soul_entity']=('A Soul Silenced','Defeat the Collector Soul.')
english['kills/uno']=('The Faceless One Falls','Defeat Uno.')

for lp in LANG.glob('*.json'):
    raw=json.loads(lp.read_text(encoding='utf-8'))
    for key,(ru,_) in TITLES.items():
        parts=key.split('.')
        section='.'.join(parts[2:-1]); suffix=parts[-1]
        en_title,en_desc=english.get(section,(ru,ru if suffix=='title' else raw.get(key,ru)))
        val=(ru if suffix=='title' else TITLES[key][0]) if lp.name=='ru_ru.json' else (en_title if suffix=='title' else en_desc)
        # If no English desc yet, avoid untranslated Russian in other locales.
        if lp.name!='ru_ru.json' and val==ru and suffix=='description':
            val='Complete the Collector challenge: '+section.replace('/',' / ').replace('_',' ')+'.'
        if lp.name!='ru_ru.json' and suffix=='title' and val==ru and section not in english:
            val=section.replace('_',' ').replace('/',' - ').title()
        raw[key]=val
    lp.write_text(json.dumps(raw,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
print('ADVANCEMENTS:',COUNT)
print('RARITY:',dict(by_type))
print('MANIFEST:',manifest)
print('ITEMS CHECK:',len(animals),len(list(ADV.rglob('*.json'))))
