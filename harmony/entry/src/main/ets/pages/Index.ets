@Entry
@Component
struct Index {
  build() {
    Column() {
      // 状态栏占位
      Row().height('8%')

      // Logo
      Row() {
        Text('N')
          .fontSize(36)
          .fontWeight(FontWeight.Bold)
          .fontColor(Color.White)
      }
      .width(80)
      .height(80)
      .justifyContent(FlexAlign.Center)
      .borderRadius(20)
      .linearGradient({
        direction: GradientDirection.TopLeft,
        colors: [['#0052D9', 0], ['#618DFF', 1]]
      })
      .margin({ bottom: 24 })

      // 标题
      Text('NeoApp')
        .fontSize(28)
        .fontWeight(FontWeight.Bold)
        .fontColor('#1D2129')
        .margin({ bottom: 12 })

      // 描述
      Text('让业务管理更简单高效')
        .fontSize(15)
        .fontColor('#86909C')
        .margin({ bottom: 48 })

      // 特性卡片
      Column({ space: 12 }) {
        FeatureCard({ emoji: '⚡', title: '高效管理', desc: '直观操作界面，轻松管理业务' })
        FeatureCard({ emoji: '🛡️', title: '安全可靠', desc: '多层安全防护，数据无忧' })
        FeatureCard({ emoji: '📊', title: '数据分析', desc: '数据可视化，辅助决策' })
        FeatureCard({ emoji: '👥', title: '团队协作', desc: '灵活权限管理，高效协作' })
      }
      .padding({ left: 32, right: 32 })
      .width('100%')

      Blank()

      // 版本号
      Text('v1.0.0')
        .fontSize(12)
        .fontColor('#86909C')
        .margin({ bottom: 24 })
    }
    .width('100%')
    .height('100%')
    .backgroundColor('#F3F3F3')
  }
}

@Component
struct FeatureCard {
  @Prop emoji: string = ''
  @Prop title: string = ''
  @Prop desc: string = ''

  build() {
    Row() {
      Text(this.emoji)
        .fontSize(28)
        .width(52)
        .height(52)
        .textAlign(TextAlign.Center)
        .backgroundColor('#FFFFFF')
        .borderRadius(14)
        .margin({ right: 16 })

      Column() {
        Text(this.title)
          .fontSize(16)
          .fontWeight(FontWeight.Medium)
          .fontColor('#1D2129')
          .margin({ bottom: 4 })

        Text(this.desc)
          .fontSize(13)
          .fontColor('#86909C')
          .maxLines(2)
          .textOverflow({ overflow: TextOverflow.Ellipsis })
      }
      .alignItems(HorizontalAlign.Start)
    }
    .width('100%')
    .padding(16)
    .backgroundColor('#FFFFFF')
    .borderRadius(16)
    .shadow({
      radius: 8,
      color: '#00000010',
      offsetX: 0,
      offsetY: 2
    })
  }
}
